package com.vocaby.application.core.util

interface EntityConverter<Entity, DomainModel> {
    suspend fun convertFromEntity(entity: Entity): DomainModel
}