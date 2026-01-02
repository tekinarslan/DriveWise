import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        LoggingInit.shared.doInitLogging()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}