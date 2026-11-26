# KaamVerse

## Setup
1. Run `database/schema.sql`, then `database/seed.sql`, in MySQL. Both are safe to run again.
2. Copy `src/main/resources/db.properties.example` to `db.properties` and set your password.
   `db.properties` is in `.gitignore`; commit only the example.
3. Run with `mvn javafx:run`, or in an IDE run `kaamverse.ui.Launcher`.

Eclipse: File > Import > Maven > Existing Maven Projects. After changing pom.xml, right-click the project > Maven > Update Project.

## Demo logins (test data only)

| Role | Username / phone | Password / PIN |
|---|---|---|
| Officer | `wage` | `officer123` |
| Officer | `verify` | `officer123` |
| Officer | `welfare` | `officer123` |
| Officer | `moderator` | `officer123` |
| Officer | `complaints` | `officer123` |
| Maid (Verified) | `03000000001` | `1111` |
| Maid (Verified) | `03000000002` | `2222` |
| Maid (Pending verification) | `03000000003` | `3333` |
| Hiring woman | `03110000001` | `4444` |
| Hiring woman | `03110000002` | `5555` |

## Menu

| Use case | Who sees it | File |
|---|---|---|
| UC1 Register as a maid | Anyone, from the login screen | `MenuModuleA` |
| UC2 Verify worker identity | `VERIFY_MAIDS` | `MenuModuleA` |
| UC3 Find and hire a maid | Hiring woman | `MenuModuleB` |
| UC4 Share past experience | Hiring woman | `MenuModuleB` |
| UC5 Reviews and trust badges | `AWARD_BADGES` | `MenuModuleB` |
| UC6 Set standard wage bands | `MANAGE_WAGE_BANDS` | `MenuModuleB` |
| UC7 My availability | Maid | `MenuModuleA` |
| UC8 Maid family records | `MANAGE_FAMILY_RECORDS` | `MenuModuleA` |
| UC9 Request event funding | Maid | `MenuModuleA` |
| UC10 Contribute to a funding request | Hiring woman | `MenuModuleA` |
| UC11 File a confidential complaint | Maid | `MenuModuleB` |
| UC12 Review harassment complaints | `REVIEW_COMPLAINTS` | `MenuModuleB` |

The menu only controls what is shown. Each use case's controller must still check its own
preconditions, such as the maid being Verified.

## How Person B adds a screen

Example: UC3.

1. Create `src/main/java/kaamverse/ui/FindMaidScreen.java`:

```java
package kaamverse.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import kaamverse.common.Strings;

public class FindMaidScreen extends VBox {

    public FindMaidScreen(SceneNavigator navigator) {
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        getChildren().add(back);
    }
}
```

2. In `ui/MenuModuleB.java`, change only the UC3 entry:

```java
MenuEntry.forRole(UserRole.HIRING_WOMAN, Strings.UC3_HIRE,
        navigator -> new FindMaidScreen(navigator))
```

3. Add any new text to the Person B section of `common/Strings.java`.
4. Keep the layers: screen -> controller (`bl/controller`) -> model or service -> DAO (`db/dao`).
   A screen never imports from `kaamverse.db`.
5. For a new DAO, add an interface and a `Jdbc...` class, then one method in `DaoFactory`.

Shared files (`Strings`, `DaoFactory`, models, schema) can cause merge conflicts, so pull before editing them.

Useful calls:
- Logged-in user, from a controller: `Session.getUser()`
- Notify someone: `new NotificationService().send(user, text)`
- Record an officer action: `AuditLogEntry.log(officer, action)`
- Several saves as one: `TransactionManager.run(() -> { ... })`

