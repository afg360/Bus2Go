package dev.mainhq.bus2go

import dev.mainhq.bus2go.di.DebugAppContainer

class DebugBus2GoApplication: Bus2GoApplication() {
	override fun onCreate() {
		super.onCreate()
		super.appContainer = DebugAppContainer(applicationContext)
	}
}