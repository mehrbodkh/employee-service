package com.mehrbod.controller.di

import com.mehrbod.controller.BaseController
import com.mehrbod.controller.EmployeeController
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton


val controllerModule = DI.Module("controller") {
    bind<EmployeeController>() with singleton {
        EmployeeController(instance())
    }

    bind<List<BaseController>>() with singleton {
        listOf(
            instance<EmployeeController>()
        )
    }
}