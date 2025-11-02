package com.mehrbod.service.di

import com.mehrbod.service.EmployeeService
import com.mehrbod.service.OrganizationService
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val serviceModule = DI.Module("serviceModule") {
    bindSingleton { EmployeeService(instance()) }
    bindSingleton { OrganizationService(instance()) }
}