package com.drivewise.app

import com.drivewise.app.db.AppDatabase

class DatabaseProvider(driverFactory: DriverFactory) {
    val db: AppDatabase = AppDatabase(driverFactory.createDriver())
}
