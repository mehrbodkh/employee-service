package com.mehrbod.data.di

import com.mehrbod.data.repository.EmployeeRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val repositoryModule = DI.Module("RepositoryModule") {
    bind<EmployeeRepository>() with singleton {
        EmployeeRepository(instance("database"), instance("io"))
    }
}