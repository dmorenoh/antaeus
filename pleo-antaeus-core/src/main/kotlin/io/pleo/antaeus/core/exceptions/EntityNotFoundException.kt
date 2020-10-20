package io.pleo.antaeus.core.exceptions

abstract class EntityNotFoundException(entity: String, id: Int) : RuntimeException("$entity '$id' was not found")
