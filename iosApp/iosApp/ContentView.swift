import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ZStack {
            // Same colour as the LaunchBackground asset and Compose root view —
            // covers any flash before the first Compose frame is drawn.
            Color(red: 0x0B / 255.0,
                  green: 0x37 / 255.0,
                  blue: 0x54 / 255.0)
                .ignoresSafeArea()
            ComposeView()
                .ignoresSafeArea()
        }
    }
}
