package com.mehrbod.controller.di

import com.mehrbod.controller.BaseController
import com.mehrbod.controller.EmployeeController
import org.kodein.di.*


val controllerModule = DI.Module("controller") {
    bindSingleton { EmployeeController(instance()) }

    bind<List<BaseController>>() with singleton {
        listOf(
            instance<EmployeeController>()
        )
    }
}