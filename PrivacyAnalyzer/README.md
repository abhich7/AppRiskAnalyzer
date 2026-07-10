# Privacy Analyzer

```
.
├── index.html              → website (Vercel deploys this, root = site)
├── PrivacyAnalyzer.apk     → the file the Download button serves
├── vercel.json             → tells Vercel this is a static site + sets APK headers
├── .gitignore
└── android-app/            → full Android Studio project (source of the APK above)
```

## Push to GitHub

```bash
cd PrivacyAnalyzer          # the folder this README is in
git init
git add .
git commit -m "Initial commit: Privacy Analyzer website + Android app"
```

Create an empty repo on GitHub first (github.com → **New repository** → don't
initialize with a README), then:

```bash
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

## Deploy to Vercel

**Option A — Dashboard (no CLI needed)**
1. Go to vercel.com → log in → **Add New... → Project**
2. **Import** the GitHub repo you just pushed
3. Leave every setting on default (Framework Preset: **Other**, Root Directory: `./`)
4. Click **Deploy**

Vercel finds `index.html` at the root and `vercel.json` and serves it as a static
site automatically — no build command needed.

**Option B — CLI**
```bash
npm i -g vercel
cd PrivacyAnalyzer
vercel --prod
```
Follow the prompts (link to your Vercel account, confirm project settings), and it
deploys straight from your local folder.

Either way you get a live URL like `your-project.vercel.app` — the **Download APK**
button on the page points to `/PrivacyAnalyzer.apk`, which sits right next to
`index.html`, so it works immediately with no extra configuration.

## Updating the APK later

1. Rebuild in `android-app/` (Android Studio → Build → Generate Signed Bundle / APK)
2. Copy the new `app-release.apk` over the root `PrivacyAnalyzer.apk`, keeping the same filename
3. `git add PrivacyAnalyzer.apk && git commit -m "Update APK" && git push`
4. Vercel auto-redeploys on every push to `main` (if you used the dashboard import) — no manual redeploy step needed.

## Note on the Android project

`android-app/` doesn't include the Gradle wrapper `.jar` binary (needs to be
downloaded, and this environment has no internet access to fetch it). Open the
folder in Android Studio and it will generate the wrapper automatically on first
sync — or run `gradle wrapper` once yourself if you use the command line.

Your signing keystore (`.jks` file) is intentionally excluded by `.gitignore` —
never commit it. Keep it somewhere safe outside the repo; you need the exact same
one for every future update of the app.
