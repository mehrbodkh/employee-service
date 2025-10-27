package com.mehrbod.di

import com.mehrbod.data.di.dataModule
import com.mehrbod.data.di.repositoryModule
import io.ktor.server.application.*
import org.kodein.di.ktor.di


fun Application.configureDI() = di {
    import(dataModule)
    import(repositoryModule)
}