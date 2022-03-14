package com.vocaby.application.feature_dictionary_custom.presentation.type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.states.ItemState
import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.GetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ModifyTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ResetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ValidateTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TypeManagementViewModel @Inject constructor(
    private val resetTypesUseCase: ResetTypesUseCase,
    private val getTypesUseCase: GetTypesUseCase,
    private val validateTypeUseCase: ValidateTypeUseCase,
    private val modifyTypesUseCase: ModifyTypesUseCase
) : ViewModel() {
    private var initTypes: HashMap<String, Type> = HashMap()
    val typeChangeState: ItemChangeState<Type> = ItemChangeState()

    private val _typeState = MutableStateFlow<MutableList<Type>>(ArrayList())
    private val _uiEvent = MutableSharedFlow<TypeUiEvent>()

    val typeState get() = _typeState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        getTypes()
    }

    fun getTypes() {
        viewModelScope.launch {
            initTypes = HashMap()
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
            when(val state = validateTypeUseCase(type, _typeState.value)) {
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
                    val sanitized = state.data as String
                    var typeToAdd = initTypes[sanitized]

                    if (typeToAdd != null) {
                        typeToAdd = Type(typeToAdd.type, 0, typeId = typeToAdd.typeId)
                        typeChangeState.updateExisting(typeToAdd.typeId, typeToAdd)
                    } else {
                        typeToAdd = Type(sanitized, 0, true)
                        typeChangeState.addNew(typeToAdd.type, typeToAdd)
                    }

                    _typeState.value.add(0, typeToAdd)
                    _typeState.value.forEachIndexed{ i, typeModel -> typeModel.order = i}

                    _uiEvent.emit(TypeUiEvent.UpdateAdapter(0, ItemState.ADD))
                }
                else -> {}
            }
        }
    }

    fun removeType(position: Int) {
        viewModelScope.launch {
            val typeToRemove = _typeState.value.removeAt(position)

            for (i in position until _typeState.value.size) {
                _typeState.value[i].order = i
            }

            if (typeToRemove.typeId == 0) typeChangeState.removeNew(typeToRemove.type)
            else typeChangeState.removeExisting(typeToRemove.typeId, typeToRemove)

            _uiEvent.emit(TypeUiEvent.UpdateAdapter(position, ItemState.DELETE))
        }
    }

    fun save() {
        viewModelScope.launch {
            fixOrdering()
            checkForUpdatedItems()

            val hasChanges = typeChangeState.hasChanges()
            if (hasChanges) {
                modifyTypesUseCase(typeChangeState)
            }

            _uiEvent.emit(TypeUiEvent.CloseEditor(hasChanges))
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
                    typeChangeState.removeItemUpdated(originalType.typeId)
                } else {
                    typeChangeState.putItemUpdated(
                        originalType.typeId,
                        currentType
                    )
                }
            }
        }
    }
}