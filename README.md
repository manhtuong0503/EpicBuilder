# EpicBuilder

Ứng dụng Android hỗ trợ xây **team defense** (Đấu Trường / Guild War) và tìm **team offense counter** cho game **Epic Seven** (Smilegate).

## Tính năng

### 🛡 Xây Team Defense
- Chọn chế độ: **Đấu Trường (4 hero)** hoặc **Guild War (3 hero)**.
- Chọn 1 hero trụ cột bất kỳ → app phân tích và xếp hạng các hero phù hợp nhất
  cho những slot còn lại, kèm **lý do cụ thể** cho từng gợi ý:
  - Độ mạnh phòng thủ trong meta hiện tại
  - Synergy trực tiếp giữa các cặp hero (ví dụ A.Ravi + Rimuru, Harsetti + Boss Arunka)
  - Bổ sung vai trò team đang thiếu (sát thương, trụ/hồi phục, khống chế, chống cleave)
  - Phạt trùng lặp class/hệ, thừa healer

### ⚔ Tìm Team Offense (Counter)
- Chọn **team defense của địch** cần đánh (1–4 hero).
- Chọn attacker đầu tiên → app gợi ý **lần lượt** các hero tiếp theo dựa trên:
  - Counter trực tiếp (ví dụ Briar Witch Iseria khắc Ruele/Maid Chloe)
  - Counter theo vai trò (extinction trị hồi sinh, speed-cap trị opener, strip trị buff…)
  - Khắc hệ nguyên tố (Băng > Hỏa > Thổ > Băng, Quang ↔ Ám)
  - Synergy với attacker đã chọn và cân bằng vai trò của team

## Dữ liệu

Toàn bộ dữ liệu nằm trong [`app/src/main/assets/heroes.json`](app/src/main/assets/heroes.json):

- `heroes` — ~85 hero PvP-meta (hệ, class, tốc độ, vai trò, điểm meta arena/GW/offense, ghi chú tiếng Việt)
- `synergies` — các cặp hero có synergy khi đứng chung team
- `counters` — các cặp khắc chế trực tiếp (attacker → defender)
- `tagCounters` — luật khắc chế theo vai trò (ví dụ `REVIVER` bị counter bởi `REVIVE_BLOCK`)

App hoạt động **hoàn toàn offline**. Muốn thêm hero mới hoặc cập nhật meta, chỉ cần
sửa file JSON này — không cần đổi code. Các giá trị enum hợp lệ được định nghĩa tại
`app/src/main/java/com/epicbuilder/data/model/Models.kt`.

## Build

Yêu cầu: **Android Studio** (Ladybug trở lên) hoặc Android SDK + JDK 17.

```bash
# Mở bằng Android Studio rồi bấm Run, hoặc:
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

- `minSdk 26` (Android 8.0+), `targetSdk 35`
- Kotlin 2.1 + Jetpack Compose (Material 3) + kotlinx.serialization

## Kiến trúc

```
app/src/main/java/com/epicbuilder/
├── data/
│   ├── model/Models.kt        # Hero, Element, Role… + schema JSON
│   └── HeroRepository.kt      # Nạp assets/heroes.json (singleton)
├── engine/
│   └── Advisor.kt             # DefenseAdvisor & OffenseAdvisor (chấm điểm + lý do)
└── ui/
    ├── home/                  # Màn hình chính
    ├── defense/               # Xây team defense
    ├── offense/               # Tìm team counter
    ├── components/            # HeroRow, SuggestionCard, TeamSlots, Search/Filter
    └── theme/                 # Material 3 dark theme
```

## Ghi chú

Điểm số và các mối quan hệ khắc chế được biên soạn từ meta PvP Epic Seven
(RTA/Arena/Guild War) tính đến giữa 2026 — mang tính tham khảo, không thay thế
được gear và kỹ năng draft của bạn. 😉
