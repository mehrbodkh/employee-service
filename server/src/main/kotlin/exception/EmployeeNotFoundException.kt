package com.mehrbod.exception

data class EmployeeNotFoundException(val id: String) : RuntimeException("Employee with id $id not found")