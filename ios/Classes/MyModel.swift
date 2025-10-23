import Foundation
import FamilyControls
import ManagedSettings

@available(iOS 15.0, *)
private let _MyModel = MyModel()

@available(iOS 15.0, *)
class MyModel: ObservableObject {
    let store = ManagedSettingsStore()

    @Published var selectionToDiscourage: FamilyActivitySelection {
        didSet {
            saveSelection(selectionToDiscourage, key: "selectionToDiscourage")
        }
    }

    @Published var selectionToEncourage: FamilyActivitySelection {
        didSet {
            saveSelection(selectionToEncourage, key: "selectionToEncourage")
        }
    }

    init() {
        selectionToDiscourage = Self.loadSelection(key: "selectionToDiscourage")
        selectionToEncourage = Self.loadSelection(key: "selectionToEncourage")

        // Nếu có dữ liệu đã lưu, tự áp dụng hạn chế
        if !selectionToDiscourage.applicationTokens.isEmpty || !selectionToDiscourage.categoryTokens.isEmpty {
            setShieldRestrictions()
        }
    }

    class var shared: MyModel {
        return _MyModel
    }

    func setShieldRestrictions() {
        print("setShieldRestrictions")
        let applications = selectionToDiscourage

        if applications.applicationTokens.isEmpty && applications.categoryTokens.isEmpty {
            print("empty applicationTokens & categoryTokens → remove shield")
            store.shield.applications = nil
            store.shield.applicationCategories = nil
            return
        }

        store.shield.applications = applications.applicationTokens.isEmpty ? nil : applications.applicationTokens
        store.shield.applicationCategories = applications.categoryTokens.isEmpty
            ? nil
            : ShieldSettings.ActivityCategoryPolicy.specific(applications.categoryTokens)

        print("Applied shield restrictions to selected apps.")
    }

    // MARK: - Save / Load Selection

    private func saveSelection(_ selection: FamilyActivitySelection, key: String) {
        do {
            let data = try NSKeyedArchiver.archivedData(withRootObject: selection, requiringSecureCoding: true)
            UserDefaults.standard.set(data, forKey: key)
            print("✅ Saved selection for \(key)")
        } catch {
            print("❌ Failed to save selection: \(error)")
        }
    }

    private static func loadSelection(key: String) -> FamilyActivitySelection {
        guard let data = UserDefaults.standard.data(forKey: key) else {
            return FamilyActivitySelection()
        }
        do {
            if let selection = try NSKeyedUnarchiver.unarchivedObject(ofClass: FamilyActivitySelection.self, from: data) {
                print("✅ Loaded saved selection for \(key)")
                return selection
            }
        } catch {
            print("❌ Failed to load selection: \(error)")
        }
        return FamilyActivitySelection()
    }

    // MARK: - Clear restrictions manually
    func clearRestrictions() {
        store.shield.applications = nil
        store.shield.applicationCategories = nil
        selectionToDiscourage = FamilyActivitySelection()
        saveSelection(selectionToDiscourage, key: "selectionToDiscourage")
    }
}
