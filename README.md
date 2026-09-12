# Smart Pantry Manager

An Android application that helps reduce household food waste by tracking the ingredients you
already have at home and suggesting only the recipes you can cook **right now** — no shopping
trip required.

Built in Java for **Mobile App Development 700** (Richfield Graduate Institute of Technology).

---

## What it does

- **Pantry management** — add, edit and delete ingredients with a quantity, unit and optional
  expiry date.
- **Suggested Recipes** — runs a strict-matching rule against your pantry and lists only the
  recipes you can make immediately.
- **Recipe detail** — full ingredient list and preparation method for any suggested recipe.
- **Settings** — toggle expiring-soon highlighting and choose your preferred units.

### The strict-matching rule

A recipe is only suggested when **every single ingredient it requires is present in the pantry,
in at least the required quantity**. If a recipe needs five ingredients and you have four, it
does not appear — partial matches are excluded from the suggestions list.

Matching is deliberately robust to everyday messiness rather than being a naive string compare:

- plural and singular forms are treated as the same ingredient (`tomatoes` matches `tomato`)
- common synonyms are mapped together (`mince` / `ground beef`, `aubergine` / `eggplant`)
- quantities are converted to a base unit before comparison, so 1 kg of flour satisfies a
  recipe that asks for 500 g

---

## Database

**SQLite, accessed through `SQLiteOpenHelper`.**

Chosen over Firebase and PostgreSQL because:

1. **The data is inherently local and single-user.** A pantry belongs to one person on one
   device. There is nothing to sync and no second user to share with, so a cloud database would
   add network dependency and latency without adding capability.
2. **It works offline.** The app is useful standing in your kitchen with no signal.
3. **Persistence is simple to reason about and to prove** — the database file lives on the
   device and survives the app being closed and reopened.
4. **No authentication layer is needed**, which keeps the scope on the matching logic rather
   than on account management.

### Data model

Three tables, normalised so that recipe ingredients are rows rather than a delimited string:

```
pantry_items(_id, name, name_key, quantity, unit, expiry_date)
recipes(_id, name, steps, servings, minutes)
recipe_ingredients(_id, recipe_id -> recipes(_id), name, name_key, quantity, unit)
```

`name_key` stores the normalised form of an ingredient name (`"Tomatoes"` becomes `tomato`) so
matching is a direct lookup rather than a scan with fuzzy comparison at query time.

The recipe collection is seeded with 18 recipes the first time the database is created.

---

## Setup and running

**Requirements**

- Android Studio (Ladybug or newer)
- JDK 17 or newer (Android Studio's bundled JetBrains Runtime is fine)
- Android SDK Platform 37
- A device or emulator running Android 7.0 (API 24) or newer

**Steps**

1. Clone the repository:
   ```bash
   git clone https://github.com/AronMorris26/smart-pantry-manager.git
   ```
2. Open the project folder in Android Studio and allow the Gradle sync to finish. Android Studio
   generates `local.properties` with your own SDK path — it is intentionally not committed.
3. Connect an Android device with USB debugging enabled, or start an emulator.
4. Press **Run** (or `./gradlew installDebug`).

**Running the unit tests**

```bash
./gradlew test
```

The tests cover the strict-matching rule, ingredient-name normalisation and unit conversion.

---

## Technical overview

| | |
|---|---|
| Language | Java |
| Minimum SDK | API 24 (Android 7.0) |
| Target / compile SDK | API 37 |
| Architecture | `MainActivity` hosts three fragments behind a bottom navigation bar; Add/Edit Ingredient and Recipe Detail are separate Activities launched with explicit Intents |
| Persistence | SQLite via `SQLiteOpenHelper` |
| Lists | `RecyclerView` with custom adapters |

This app uses no mapping SDK and requests no location permission.
