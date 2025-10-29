package com.mehrbod.controller.di

import com.mehrbod.controller.BaseController
import com.mehrbod.controller.EmployeeController
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton


val controllerModule = DI.Module("controller") {
    bindSingleton { EmployeeController(instance(), instance()) }

    bind<List<BaseController>>() with singleton {
        listOf(
            instance<EmployeeController>()
        )
    }
}