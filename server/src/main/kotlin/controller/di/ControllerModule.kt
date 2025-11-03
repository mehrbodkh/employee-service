package com.mehrbod.controller.di

import com.mehrbod.controller.BaseController
import com.mehrbod.controller.EmployeeController
import com.mehrbod.controller.OrganizationController
import com.mehrbod.controller.ReviewController
import org.kodein.di.*


val controllerModule = DI.Module("controller") {
    bind<List<BaseController>>() with singleton {
        listOf(
            EmployeeController(instance()),
            OrganizationController(instance()),
            ReviewController(instance())
        )
    }
}