# Pending Work / अधूरे काम — ईमानदार सूची

यह फाइल जानबूझकर सबसे ऊपर रखी गई है। इसमें वह सब कुछ है जो अभी तक **research/verify नहीं हुआ**,
**test नहीं हुआ**, या जो मैंने Android SDK/Gradle के बिना, सिर्फ code review से बनाया — यानी
"Zero Assumption Rule" और "Build Verification" के अपने ही नियम के हिसाब से, यह Phase Completion
Checklist अभी पास नहीं करता। इसे build करने से पहले पढ़ें।

## 1. यह repo अभी तक कभी compile नहीं हुआ

इस environment में Android SDK नहीं है, इसलिए `./gradlew assembleDebug` कभी नहीं चलाया जा सका।
पूरा code Kotlin/Gradle/Room/Hilt की जानकारी के आधार पर हाथ से लिखा गया है, syntax सावधानी से
जाँचा गया है, और तीन असली bugs code-review में पकड़कर ठीक किए गए (नीचे Section 3 देखें) — लेकिन
**असली compiler ने इसे अभी तक नहीं देखा।** पहला GitLab CI run ही असली सच बताएगा।

**आपको क्या करना है:** Repo push करने के बाद पहले pipeline को ध्यान से देखें। संभावित समस्याएं:
- KSP/Hilt annotation processing में version mismatch
- Room schema export path (`app/schemas/`) की ज़रूरत हो सकती है
- SQLCipher native library लोड होने में डिवाइस/emulator-विशेष दिक्कत
- `.gitlab-ci.yml` में `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` का पथ अनुमानित है (सामान्य
  Docker images में यह standard path है), पर `mingc/android-build-box` में असली path अलग हो सकता
  है — अगर pipeline में "JAVA_HOME तय नहीं मिला" जैसी error आए तो `before_script` में यह लाइन
  हटा दें (image का default JDK शायद पहले से ठीक हो) या सही path के लिए `ls /usr/lib/jvm/` चलाएँ।

## 2. Level 4 Duplicate Detection (Semantic AI) — लागू नहीं है

Master Specification Section 9 में Level 4 (Semantic AI similarity) का ज़िक्र है। यह repo सिर्फ
Level 1-3 (Hash, Metadata, Perceptual Hash) देता है। Level 4 के लिए एक असली TFLite embedding
model चाहिए (Phase 9-10) — वह model चुनना, बंडल करना, और उसका JNI/TFLite wiring खुद एक बड़ा
काम है जिसे "जल्दी में" ठीक से नहीं किया जा सकता। Roadmap के Phase 9-10 में यह अलग से होना चाहिए।

## 3. Code review में मिले और ठीक किए गए bugs (transparency के लिए)

- `moveToTrash()` में एक bug था जो trash में डाली गई फाइल का entity पुराने (अब न रहे) path से
  फिर से पढ़ने की कोशिश कर रहा था — ठीक कर दिया गया, अब पहले से मौजूद domain data इस्तेमाल होता है।
- Vault का "Biometric unlock" गलती से PIN-verification code को दोबारा (गलत तरीके से) कॉल कर
  रहा था — अब एक अलग, सही `unlockWithBiometric()` path है जो BiometricPrompt के अपने-आप में
  प्रमाणीकरण होने पर भरोसा करता है।
- `OcrRepositoryImpl` में एक placeholder/broken expression था जहाँ Context injection होना
  चाहिए था — ठीक कर दिया गया (`@ApplicationContext` अब सही से inject होता है)।
- **सबसे गंभीर:** `DatabaseModule.kt` शुरू में `net.zetetic:sqlcipher-android` (नया, maintained
  artifact) को dependency में इस्तेमाल कर रहा था, पर कोड `net.sqlcipher.database.SupportFactory`
  (पुराने, अब deprecated `android-database-sqlcipher` artifact की class) import कर रहा था — यह
  दो अलग-अलग libraries हैं, अलग Java package namespace के साथ। यह build को शुरू से ही तोड़ देता
  (`unresolved reference`)। Web search से verify करने के बाद सही किया गया — अब सही class
  `net.zetetic.database.sqlcipher.SupportOpenHelperFactory` इस्तेमाल होती है, जो असली माने में
  installed dependency से मेल खाती है।

**ईमानदार बात:** अगर मैंने इतनी सावधानी से review न किया होता, तो ये तीनों bugs सीधे repo में
चले जाते और सिर्फ runtime crash से पता चलते। CI compile-check यह नहीं पकड़ेगा (ये logic bugs
हैं, syntax bugs नहीं) — इसलिए **manual testing ज़रूरी है**, सिर्फ green pipeline पर भरोसा न करें।

## 4. Testing — बहुत कम कवरेज

सिर्फ 3 pure-logic unit test files हैं (FileSizeFormatter, PerceptualHash math, FileCategory) —
कुल ~15 test cases। यह Roadmap Phase 16 ("Unit Test, Integration Test, UI Test, Security Test,
All Tests Pass") से बहुत दूर है। Repository/ViewModel/DAO लेयर का **कोई automated test नहीं है**।
Room in-memory DB tests, Hilt test modules, और Compose UI tests — यह सब अभी बाकी है।

## 5. Release Signing — कॉन्फ़िगर नहीं है

CI release job में keystore secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD`) की जगह है, पर कोई keystore generate नहीं किया गया — क्योंकि keystore को कभी
repo या chat में generate नहीं करना चाहिए (वह production सुरक्षा का आधार है)। जब तक ये
GitLab CI/CD Variables में नहीं डाले जाते, release job अपने-आप unsigned APK बनाएगा, जो सिर्फ
internal testing के लिए ठीक है, Play Store के लिए नहीं।

## 6. Play Store "All Files Access" Declaration — नहीं भरा गया

`MANAGE_EXTERNAL_STORAGE` permission (AndroidManifest.xml) एक file manager app के लिए approved
use-case है, पर Play Console में इसके लिए एक अलग "All Files Access" declaration form भरना
अनिवार्य होता है, वरना app submission पर reject होगा। यह repo के दायरे से बाहर है — Phase 18
(Production Audit) में करना होगा।

## 7. Cloud Sync (Phase 11) — बिल्कुल शुरू नहीं हुआ

इस repo में कोई Google Drive/OneDrive/Dropbox/NextCloud/S3/NAS code नहीं है। Master Roadmap के
अनुसार यह Phase 11 है — File Manager (Phase 3-7) और Vault (Phase 6) के base ready होने के बाद
अगला काम। जानबूझकर इसे अभी शामिल नहीं किया, ताकि core पहले ठोस बने।

## 8. OCR Plugin — कोड मौजूद है, पर feature फिलहाल core में wired नहीं

`OcrRepositoryImpl` और उसका ML Kit binding बना है, पर UI में कहीं से call नहीं होता (कोई
"Extract Text" button नहीं है)। Roadmap के हिसाब से यह सही है (Phase 8 = plugin, अभी core-phase
में हैं), पर अगर आप जल्दी OCR टेस्ट करना चाहें तो अभी manual wiring करनी होगी।

## 9. Similarity slider की सीमाएं

Duplicate Cleaner का 70%-95% slider सिर्फ perceptual-hash (Level 3) पर लागू है — असली AI-आधारित
"semantic similarity" (Level 4, जैसे burst-photo detection) नहीं। बड़े photo sets (10k+ images)
पर वर्तमान O(n²) pairwise comparison धीमा पड़ सकता है — यह अभी सिर्फ correctness के लिए लिखा गया
है, बड़े scale पर optimize नहीं (जैसे locality-sensitive hashing bucket करना)।

## अगला सुझाया गया कदम

1. Repo को GitLab पर push करें, पहला pipeline run देखें, यहाँ जो errors मिलें वो share करें।
2. Debug APK को असली फोन पर install करके File Manager + Vault का manual test करें।
3. उसके बाद ही Phase 8 (OCR UI wiring), Phase 9-10 (Semantic AI), Phase 11 (Cloud) पर बढ़ें —
   Golden Rule के अनुसार "कोई भी चरण Skip नहीं होगा।"
