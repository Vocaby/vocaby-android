package com.vocaby.application.core.util

interface EntityConverter<Entity, DomainModel> {
    fun convertFromEntity(entity: Entity): DomainModel
}