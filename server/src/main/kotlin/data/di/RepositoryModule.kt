package com.mehrbod.data.di

import com.mehrbod.data.repository.EmployeeRepository
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val repositoryModule = DI.Module("RepositoryModule") {
    bindSingleton {
        EmployeeRepository(instance("database"), instance("io"))
    }
}