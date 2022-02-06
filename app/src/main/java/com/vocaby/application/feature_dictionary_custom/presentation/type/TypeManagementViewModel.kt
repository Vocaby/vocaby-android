package com.vocaby.application.feature_dictionary_custom.presentation.type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.GetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.CreateTypeUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ModifyTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ResetTypesUseCase
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class TypeManagementViewModel @Inject constructor(
    private val resetTypesUseCase: ResetTypesUseCase,
    private val getTypesUseCase: GetTypesUseCase,
    private val createTypeUseCase: CreateTypeUseCase,
    private val modifyTypesUseCase: ModifyTypesUseCase
) : ViewModel() {
    private var initTypes: HashMap<String, Type> = HashMap()
    private val typeChangeState: ItemChangeState<Type> = ItemChangeState()

    private val _typeState = MutableStateFlow<LinkedList<Type>>(LinkedList())
    private val _uiEvent = MutableSharedFlow<TypeUiEvent>()

    val typeState get() = _typeState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            val types = getTypesUseCase().first()
            types.forEach { typeModel -> initTypes[typeModel.type] = typeModel.copy() }
            _typeState.value = LinkedList(types)
        }
    }

    fun resetTypes() {
        viewModelScope.launch {
            resetTypesUseCase()
        }
    }

    fun createType(type: String) {
        viewModelScope.launch {
            when(val state = createTypeUseCase(type, _typeState.value)) {
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(TypeUiEvent.ShowAlert("Please enter a type"))
                }
                is UserInputState.InvalidInput -> {
                    _uiEvent.emit(TypeUiEvent.ShowAlert("This type contains special characters"))
                }
                is UserInputState.SameInput<*> -> {
                    _uiEvent.emit(TypeUiEvent.ShowAlert("This type already exists"))
                }
                is UserInputState.Valid<*> -> {
                    val newType: Type
                    val sanitized = state.data as String

                    if (typeChangeState.hasItemDeleted(sanitized)) {
                        // Must be one of existing types
                        newType = typeChangeState.getItemDeleted(sanitized)!!
                        newType.order = 0

                        typeChangeState.removeItemDeleted(newType.type)
                        typeChangeState.putItemUpdated(newType.type, newType)
                    } else {
                        newType = Type(sanitized, 0, true)
                        typeChangeState.addItem(newType.type, newType)
                    }

                    _typeState.value.add(0, newType)
                    _typeState.value.forEachIndexed{ i, typeModel -> typeModel.order = i}
                    _uiEvent.emit(TypeUiEvent.UpdateAdapter(0, ItemState.ADD))
                }
                else -> {}
            }
        }
    }

    fun removeType(position: Int) {
        viewModelScope.launch {
            val typeToRemove = _typeState.value[position]
            _typeState.value.removeAt(position)
            for (i in position until _typeState.value.size) {
                _typeState.value[i].order = i
            }

            typeChangeState.removeItem(typeToRemove.type, typeToRemove)
            _uiEvent.emit(TypeUiEvent.UpdateAdapter(position, ItemState.DELETE))
        }
    }

    fun save() {
        viewModelScope.launch {
            fixOrdering()
            checkForUpdatedItems()
            modifyTypesUseCase(typeChangeState)
            _uiEvent.emit(TypeUiEvent.CloseEditor)
        }
    }

    // changeState map does not hold references to typeModels.
    private fun fixOrdering() {
        for (typeModel in _typeState.value) {
            if (typeChangeState.hasItemAdded(typeModel.type)) {
                typeChangeState.putItemAdded(typeModel.type, typeModel)
            }
        }
    }

    private fun checkForUpdatedItems() {
        for (i in _typeState.value.indices) {
            val currentType = _typeState.value[i]
            val originalType = initTypes[currentType.type]
            if (originalType != null) {
                if (originalType.order == currentType.order) {
                    typeChangeState.removeItemUpdated(currentType.type)
                } else {
                    typeChangeState.putItemUpdated(
                        currentType.type,
                        currentType
                    )
                }
            }
        }
    }
}