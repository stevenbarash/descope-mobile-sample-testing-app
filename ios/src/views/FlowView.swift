import SwiftUI
import DescopeKit

struct FlowView: View {
    @Environment(AppState.self) private var appState
    @Environment(\.dismiss) private var dismiss
    @State private var error: DescopeError?

    var body: some View {
        FlowViewRepresentable(
            onSuccess: { response in
                appState.setSession(DescopeSession(from: response))
            },
            onError: { error = $0 },
            onCancel: { dismiss() }
        )
        .ignoresSafeArea()
        .navigationTitle("Sign In")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: .init(
            get: { error != nil },
            set: { if !$0 { error = nil } },
        )) {
            Button("OK") { error = nil }
        } message: {
            Text(error?.description ?? "An error occurred")
        }
    }
}

private struct FlowViewRepresentable: UIViewControllerRepresentable {
    let onSuccess: (AuthenticationResponse) -> Void
    let onError: (DescopeError) -> Void
    let onCancel: () -> Void

    func makeUIViewController(context: Context) -> DescopeFlowViewController {
        let controller = DescopeFlowViewController()
        controller.delegate = context.coordinator
        let url = "\(AppConfig.baseURL)/login/\(AppConfig.projectId)?mobile=true&flow=\(AppConfig.flowId)"
        controller.start(flow: DescopeFlow(url: url))
        return controller
    }

    func updateUIViewController(_ controller: DescopeFlowViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onSuccess: onSuccess, onError: onError, onCancel: onCancel)
    }

    final class Coordinator: NSObject, DescopeFlowViewControllerDelegate {
        let onSuccess: (AuthenticationResponse) -> Void
        let onError: (DescopeError) -> Void
        let onCancel: () -> Void

        init(onSuccess: @escaping (AuthenticationResponse) -> Void, onError: @escaping (DescopeError) -> Void, onCancel: @escaping () -> Void) {
            self.onSuccess = onSuccess
            self.onError = onError
            self.onCancel = onCancel
        }

        func flowViewControllerDidUpdateState(_ controller: DescopeFlowViewController, to state: DescopeFlowState, from previous: DescopeFlowState) {}
        func flowViewControllerDidBecomeReady(_ controller: DescopeFlowViewController) {}
        func flowViewControllerShouldShowURL(_ controller: DescopeFlowViewController, url: URL, external: Bool) -> Bool { true }
        func flowViewControllerDidCancel(_ controller: DescopeFlowViewController) { onCancel() }
        func flowViewControllerDidFail(_ controller: DescopeFlowViewController, error: DescopeError) { onError(error) }
        func flowViewControllerDidFinish(_ controller: DescopeFlowViewController, response: AuthenticationResponse) { onSuccess(response) }
    }
}
