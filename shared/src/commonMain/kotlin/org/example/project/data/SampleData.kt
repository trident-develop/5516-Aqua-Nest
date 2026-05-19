package org.example.project.data

import androidx.compose.ui.graphics.Color
import org.example.project.theme.AquariumColors

data class FishItem(
    val name: String,
    val type: String,
    val careLevel: String,
    val compatibility: Int,
    val accent: Color,
    val category: FishCategory,
    val sizeCm: String = "5–10 cm",
    val tempC: String = "22–26 °C",
    val phRange: String = "6.5–7.5",
    val lifespanYears: String = "3–5 years",
    val diet: String = "Omnivore",
    val origin: String = "Various",
    val description: String = "",
)

enum class FishCategory(val label: String) {
    Freshwater("Freshwater"),
    Saltwater("Saltwater"),
    Peaceful("Peaceful"),
    Sensitive("Sensitive"),
}

data class TankInfo(
    val name: String,
    val sizeLiters: Int,
    val fishCount: Int,
    val waterType: String,
    val active: Boolean,
)

data class DeviceItem(
    val name: String,
    val glyph: String,
    val initiallyOn: Boolean,
    val description: String,
)

data class EquipmentItem(
    val name: String,
    val status: String,
    val glyph: String,
)

data class MaintenanceTask(
    val title: String,
    val initiallyDone: Boolean,
)

data class FeedingEntry(
    val time: String,
    val food: String,
    val portion: String,
    val done: Boolean,
)

data class WaterReading(
    val label: String,
    val value: String,
    val unit: String,
    val accent: Color,
    val trend: List<Float>,
)

object SampleData {
    val fish = listOf(
        FishItem(
            name = "Neon Tetra", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.LightAqua, category = FishCategory.Freshwater,
            sizeCm = "3–4 cm", tempC = "20–26 °C", phRange = "5.0–7.0",
            lifespanYears = "5–8 years", diet = "Omnivore", origin = "Amazon Basin",
            description = "Iconic schooling fish with a shimmering blue-and-red stripe. Peaceful, best kept in groups of 6+.",
        ),
        FishItem(
            name = "Clownfish", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.Warning, category = FishCategory.Saltwater,
            sizeCm = "8–11 cm", tempC = "24–27 °C", phRange = "8.1–8.4",
            lifespanYears = "6–10 years", diet = "Carnivore", origin = "Pacific & Indian Oceans",
            description = "Hardy reef fish famous for its anemone symbiosis. Bold and territorial in small tanks.",
        ),
        FishItem(
            name = "Betta", type = "Freshwater", careLevel = "Easy", compatibility = 1,
            accent = AquariumColors.Danger, category = FishCategory.Freshwater,
            sizeCm = "6–8 cm", tempC = "24–28 °C", phRange = "6.5–7.5",
            lifespanYears = "3–5 years", diet = "Carnivore", origin = "Thailand, Cambodia",
            description = "Stunning long-finned labyrinth fish. Males must be kept alone — extremely aggressive to other bettas.",
        ),
        FishItem(
            name = "Angelfish", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.PaleAqua, category = FishCategory.Peaceful,
            sizeCm = "12–15 cm", tempC = "24–28 °C", phRange = "6.0–7.5",
            lifespanYears = "10–12 years", diet = "Omnivore", origin = "Amazon Basin",
            description = "Tall, graceful cichlid. Generally peaceful but may nip small tetras and fin-trail fish.",
        ),
        FishItem(
            name = "Discus", type = "Freshwater", careLevel = "Hard", compatibility = 1,
            accent = AquariumColors.SoftLime, category = FishCategory.Sensitive,
            sizeCm = "15–20 cm", tempC = "28–30 °C", phRange = "6.0–7.0",
            lifespanYears = "10–15 years", diet = "Omnivore", origin = "Amazon River",
            description = "Round-bodied 'king of the aquarium'. Demands warm, soft water and pristine conditions.",
        ),
        FishItem(
            name = "Guppy", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Lime, category = FishCategory.Peaceful,
            sizeCm = "3–6 cm", tempC = "22–28 °C", phRange = "6.8–7.8",
            lifespanYears = "2–3 years", diet = "Omnivore", origin = "South America",
            description = "Colorful livebearer that breeds prolifically. Ideal beginner fish.",
        ),
        FishItem(
            name = "Mandarin", type = "Saltwater", careLevel = "Hard", compatibility = 1,
            accent = AquariumColors.LimeDeep, category = FishCategory.Sensitive,
            sizeCm = "6–8 cm", tempC = "24–26 °C", phRange = "8.1–8.4",
            lifespanYears = "5–8 years", diet = "Carnivore (copepods)", origin = "Pacific Ocean reefs",
            description = "Psychedelic patterned dragonet. Picky eater that needs an established reef rich in live copepods.",
        ),
        FishItem(
            name = "Tang", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.LightAqua, category = FishCategory.Saltwater,
            sizeCm = "15–25 cm", tempC = "24–27 °C", phRange = "8.1–8.4",
            lifespanYears = "8–12 years", diet = "Herbivore", origin = "Indo-Pacific reefs",
            description = "Active swimmer that needs lots of room. Loves algae sheets and open water to cruise.",
        ),
        FishItem(
            name = "Cardinal Tetra", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Danger, category = FishCategory.Peaceful,
            sizeCm = "3 cm", tempC = "23–27 °C", phRange = "4.6–6.2",
            lifespanYears = "4–5 years", diet = "Omnivore", origin = "Orinoco, Negro rivers",
            description = "Like a neon tetra but with a red stripe along its entire belly. Schooling fish for soft water.",
        ),
        FishItem(
            name = "Rummy Nose Tetra", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "4–5 cm", tempC = "22–27 °C", phRange = "5.5–7.0",
            lifespanYears = "5–6 years", diet = "Omnivore", origin = "Amazon Basin",
            description = "Tight-schooling tetra with a brilliant red nose. The 'fade' of the red is a stress indicator.",
        ),
        FishItem(
            name = "Glowlight Tetra", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "4 cm", tempC = "22–28 °C", phRange = "5.8–7.5",
            lifespanYears = "4–5 years", diet = "Omnivore", origin = "Guyana",
            description = "Subtle, peaceful tetra with a glowing orange stripe. Looks best against dark substrate.",
        ),
        FishItem(
            name = "Black Skirt Tetra", type = "Freshwater", careLevel = "Easy", compatibility = 2,
            accent = AquariumColors.MutedAqua, category = FishCategory.Freshwater,
            sizeCm = "5–7 cm", tempC = "22–27 °C", phRange = "6.0–7.5",
            lifespanYears = "5 years", diet = "Omnivore", origin = "Paraguay",
            description = "Tall tetra with dark dorsal half. Hardy but can nip long-finned tankmates if kept in small groups.",
        ),
        FishItem(
            name = "Harlequin Rasbora", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "4–5 cm", tempC = "22–27 °C", phRange = "6.0–7.0",
            lifespanYears = "5–8 years", diet = "Omnivore", origin = "Malaysia, Sumatra",
            description = "Copper-orange schooling fish with a black triangular patch. Excellent community tank species.",
        ),
        FishItem(
            name = "Cherry Barb", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Danger, category = FishCategory.Peaceful,
            sizeCm = "5 cm", tempC = "22–27 °C", phRange = "6.0–7.5",
            lifespanYears = "5–7 years", diet = "Omnivore", origin = "Sri Lanka",
            description = "Bright red males. Calm, easy barb suitable for planted community tanks.",
        ),
        FishItem(
            name = "Tiger Barb", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.Warning, category = FishCategory.Freshwater,
            sizeCm = "6–8 cm", tempC = "24–28 °C", phRange = "6.0–7.5",
            lifespanYears = "5–7 years", diet = "Omnivore", origin = "Borneo, Sumatra",
            description = "Striking striped barb. Notorious fin-nipper if kept in groups smaller than 8.",
        ),
        FishItem(
            name = "Zebra Danio", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.LightAqua, category = FishCategory.Peaceful,
            sizeCm = "5 cm", tempC = "18–25 °C", phRange = "6.5–7.5",
            lifespanYears = "3–5 years", diet = "Omnivore", origin = "South Asia",
            description = "Active, hardy striped fish. Tolerates cooler water and is ideal for beginners.",
        ),
        FishItem(
            name = "Pearl Danio", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.PaleAqua, category = FishCategory.Peaceful,
            sizeCm = "5–6 cm", tempC = "20–26 °C", phRange = "6.5–7.5",
            lifespanYears = "5 years", diet = "Omnivore", origin = "Myanmar, Thailand",
            description = "Iridescent pearly sheen with subtle stripes. Very active schooler.",
        ),
        FishItem(
            name = "Corydoras Catfish", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.MutedAqua, category = FishCategory.Peaceful,
            sizeCm = "5–7 cm", tempC = "22–26 °C", phRange = "6.0–7.5",
            lifespanYears = "5–10 years", diet = "Omnivore (sinking)", origin = "South America",
            description = "Adorable bottom-dwelling catfish. Keep in groups of 5+, on smooth sand.",
        ),
        FishItem(
            name = "Otocinclus", type = "Freshwater", careLevel = "Medium", compatibility = 3,
            accent = AquariumColors.SoftLime, category = FishCategory.Sensitive,
            sizeCm = "3–4 cm", tempC = "21–27 °C", phRange = "6.0–7.5",
            lifespanYears = "3–5 years", diet = "Herbivore", origin = "South America",
            description = "Tiny algae-eating catfish. Needs a mature tank with biofilm and soft greens.",
        ),
        FishItem(
            name = "Bristlenose Pleco", type = "Freshwater", careLevel = "Easy", compatibility = 2,
            accent = AquariumColors.MutedAqua, category = FishCategory.Freshwater,
            sizeCm = "10–15 cm", tempC = "23–27 °C", phRange = "6.5–7.5",
            lifespanYears = "10–15 years", diet = "Herbivore", origin = "Amazon Basin",
            description = "Small armored algae-eater. Males develop characteristic facial bristles.",
        ),
        FishItem(
            name = "Royal Pleco", type = "Freshwater", careLevel = "Medium", compatibility = 1,
            accent = AquariumColors.PaleAqua, category = FishCategory.Freshwater,
            sizeCm = "30–40 cm", tempC = "24–30 °C", phRange = "6.5–7.5",
            lifespanYears = "10+ years", diet = "Herbivore (wood)", origin = "Orinoco",
            description = "Stunning striped large pleco. Needs driftwood to graze on and a large tank.",
        ),
        FishItem(
            name = "Apistogramma", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "5–8 cm", tempC = "23–28 °C", phRange = "5.5–7.0",
            lifespanYears = "5 years", diet = "Carnivore", origin = "Amazon",
            description = "Dwarf cichlid with vivid colors. Forms breeding pairs and is good in nano cichlid tanks.",
        ),
        FishItem(
            name = "Ram Cichlid", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.SoftLime, category = FishCategory.Peaceful,
            sizeCm = "5–7 cm", tempC = "25–29 °C", phRange = "5.5–7.0",
            lifespanYears = "2–3 years", diet = "Omnivore", origin = "Venezuela, Colombia",
            description = "Brilliantly colored dwarf cichlid. Wants warm, very clean water.",
        ),
        FishItem(
            name = "Convict Cichlid", type = "Freshwater", careLevel = "Easy", compatibility = 1,
            accent = AquariumColors.MutedAqua, category = FishCategory.Freshwater,
            sizeCm = "10–15 cm", tempC = "20–28 °C", phRange = "6.5–8.0",
            lifespanYears = "8–10 years", diet = "Omnivore", origin = "Central America",
            description = "Bold and very territorial. Best in a species-only or carefully picked rough community.",
        ),
        FishItem(
            name = "Oscar", type = "Freshwater", careLevel = "Medium", compatibility = 1,
            accent = AquariumColors.Warning, category = FishCategory.Freshwater,
            sizeCm = "30–35 cm", tempC = "23–27 °C", phRange = "6.0–8.0",
            lifespanYears = "10–15 years", diet = "Carnivore", origin = "Amazon",
            description = "Charismatic 'wet pet' cichlid that recognizes its owner. Needs a 280 L+ tank.",
        ),
        FishItem(
            name = "Jack Dempsey", type = "Freshwater", careLevel = "Medium", compatibility = 1,
            accent = AquariumColors.LightAqua, category = FishCategory.Freshwater,
            sizeCm = "20–25 cm", tempC = "22–28 °C", phRange = "6.5–7.5",
            lifespanYears = "8–10 years", diet = "Carnivore", origin = "Central America",
            description = "Aggressive electric-blue cichlid. Stunning when mature, hard on tankmates.",
        ),
        FishItem(
            name = "Severum", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.SoftLime, category = FishCategory.Freshwater,
            sizeCm = "20 cm", tempC = "24–28 °C", phRange = "6.0–7.5",
            lifespanYears = "10 years", diet = "Omnivore", origin = "South America",
            description = "Calmer 'gentle giant' cichlid. Great for larger community setups with similarly sized fish.",
        ),
        FishItem(
            name = "Pearl Gourami", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.PaleAqua, category = FishCategory.Peaceful,
            sizeCm = "10–12 cm", tempC = "24–28 °C", phRange = "6.0–7.5",
            lifespanYears = "4–5 years", diet = "Omnivore", origin = "Southeast Asia",
            description = "Elegant labyrinth fish covered in pearl-like spots. Peaceful and beautiful.",
        ),
        FishItem(
            name = "Dwarf Gourami", type = "Freshwater", careLevel = "Easy", compatibility = 2,
            accent = AquariumColors.Danger, category = FishCategory.Peaceful,
            sizeCm = "7–8 cm", tempC = "24–28 °C", phRange = "6.0–7.5",
            lifespanYears = "4 years", diet = "Omnivore", origin = "India, Bangladesh",
            description = "Bright red or blue centerpiece for community tanks. Keep single males to avoid fights.",
        ),
        FishItem(
            name = "Honey Gourami", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "5 cm", tempC = "22–28 °C", phRange = "6.0–7.5",
            lifespanYears = "4–5 years", diet = "Omnivore", origin = "India",
            description = "Mellow nano-sized gourami with a warm honey color. Calm in any community tank.",
        ),
        FishItem(
            name = "Endler Guppy", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.SoftLime, category = FishCategory.Peaceful,
            sizeCm = "3 cm", tempC = "22–28 °C", phRange = "7.0–8.0",
            lifespanYears = "2–3 years", diet = "Omnivore", origin = "Venezuela",
            description = "Smaller, jewel-bright cousin of the common guppy. Breeds quickly in any setup.",
        ),
        FishItem(
            name = "Platy", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "5–6 cm", tempC = "20–26 °C", phRange = "7.0–8.0",
            lifespanYears = "3–4 years", diet = "Omnivore", origin = "Central America",
            description = "Easy livebearer with many color morphs. Tolerant and active.",
        ),
        FishItem(
            name = "Swordtail", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Danger, category = FishCategory.Peaceful,
            sizeCm = "10–14 cm", tempC = "22–26 °C", phRange = "7.0–8.0",
            lifespanYears = "3–5 years", diet = "Omnivore", origin = "Central America",
            description = "Energetic livebearer; males sport a long sword-shaped lower tail.",
        ),
        FishItem(
            name = "Molly", type = "Freshwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.MutedAqua, category = FishCategory.Peaceful,
            sizeCm = "8–10 cm", tempC = "22–28 °C", phRange = "7.5–8.5",
            lifespanYears = "3–5 years", diet = "Omnivore", origin = "Central America",
            description = "Hardy livebearer that appreciates slightly hard, alkaline water.",
        ),
        FishItem(
            name = "Killifish", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.Warning, category = FishCategory.Freshwater,
            sizeCm = "5–7 cm", tempC = "20–24 °C", phRange = "6.0–7.0",
            lifespanYears = "2–3 years", diet = "Carnivore", origin = "Africa, S. America",
            description = "Jewel-like tropical 'annual' fish. Short-lived but extremely colorful.",
        ),
        FishItem(
            name = "Rainbow Shark", type = "Freshwater", careLevel = "Medium", compatibility = 1,
            accent = AquariumColors.Danger, category = FishCategory.Freshwater,
            sizeCm = "12–15 cm", tempC = "24–27 °C", phRange = "6.5–7.5",
            lifespanYears = "5–8 years", diet = "Omnivore", origin = "Southeast Asia",
            description = "Sleek black body with bright red fins. Territorial — best as the only 'shark' in the tank.",
        ),
        FishItem(
            name = "Bala Shark", type = "Freshwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.PaleAqua, category = FishCategory.Freshwater,
            sizeCm = "25–35 cm", tempC = "22–28 °C", phRange = "6.5–7.5",
            lifespanYears = "10 years", diet = "Omnivore", origin = "Borneo, Sumatra",
            description = "Active silver schooler. Outgrows most tanks fast — needs 300 L+ when adult.",
        ),
        FishItem(
            name = "Hatchetfish", type = "Freshwater", careLevel = "Medium", compatibility = 3,
            accent = AquariumColors.MutedAqua, category = FishCategory.Peaceful,
            sizeCm = "4–6 cm", tempC = "23–27 °C", phRange = "5.5–7.0",
            lifespanYears = "3–5 years", diet = "Omnivore (surface)", origin = "Amazon",
            description = "Surface-dwelling fish with a deep keel. A tight lid is mandatory — they jump.",
        ),
        FishItem(
            name = "Pencilfish", type = "Freshwater", careLevel = "Medium", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "4–5 cm", tempC = "23–28 °C", phRange = "5.5–7.0",
            lifespanYears = "3–5 years", diet = "Omnivore (small)", origin = "South America",
            description = "Slender, peaceful fish that swims tilted. Ideal for blackwater aquascapes.",
        ),
        FishItem(
            name = "Glass Catfish", type = "Freshwater", careLevel = "Medium", compatibility = 3,
            accent = AquariumColors.PaleAqua, category = FishCategory.Peaceful,
            sizeCm = "10 cm", tempC = "23–27 °C", phRange = "6.5–7.5",
            lifespanYears = "7–8 years", diet = "Carnivore", origin = "Thailand",
            description = "Transparent shoaling catfish — you can see the spine! Keep in groups of 6+.",
        ),
        FishItem(
            name = "Yellow Tang", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.Warning, category = FishCategory.Saltwater,
            sizeCm = "15–20 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "8–10 years", diet = "Herbivore", origin = "Hawaii",
            description = "Vibrant yellow algae grazer. Common reef pick that needs space to cruise.",
        ),
        FishItem(
            name = "Powder Blue Tang", type = "Saltwater", careLevel = "Hard", compatibility = 1,
            accent = AquariumColors.LightAqua, category = FishCategory.Sensitive,
            sizeCm = "20–23 cm", tempC = "24–27 °C", phRange = "8.1–8.4",
            lifespanYears = "7–9 years", diet = "Herbivore", origin = "Indian Ocean",
            description = "Stunning blue body with yellow trim. Prone to ich; needs pristine water and lots of room.",
        ),
        FishItem(
            name = "Sailfin Tang", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.SoftLime, category = FishCategory.Saltwater,
            sizeCm = "30–40 cm", tempC = "24–27 °C", phRange = "8.1–8.4",
            lifespanYears = "10+ years", diet = "Herbivore", origin = "Indo-Pacific",
            description = "Huge dorsal fin that fans out when threatened. Wants a very large reef tank.",
        ),
        FishItem(
            name = "Six-Line Wrasse", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.LimeDeep, category = FishCategory.Saltwater,
            sizeCm = "8–10 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "5–10 years", diet = "Carnivore", origin = "Indo-Pacific reefs",
            description = "Active wrasse with bold horizontal lines. Can be a pest controller for flatworms.",
        ),
        FishItem(
            name = "Royal Gramma", type = "Saltwater", careLevel = "Easy", compatibility = 2,
            accent = AquariumColors.Danger, category = FishCategory.Saltwater,
            sizeCm = "8 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "5 years", diet = "Carnivore", origin = "Caribbean",
            description = "Half purple, half yellow. Hardy and territorial about a single cave.",
        ),
        FishItem(
            name = "Firefish Goby", type = "Saltwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.Warning, category = FishCategory.Peaceful,
            sizeCm = "6–8 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "3–5 years", diet = "Carnivore", origin = "Indo-Pacific reefs",
            description = "Slim hovering goby with a flame-tipped tail. Shy at first — provide caves.",
        ),
        FishItem(
            name = "Banggai Cardinal", type = "Saltwater", careLevel = "Medium", compatibility = 2,
            accent = AquariumColors.PaleAqua, category = FishCategory.Saltwater,
            sizeCm = "7–8 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "4–5 years", diet = "Carnivore", origin = "Banggai Islands",
            description = "Elegant black-and-silver cardinalfish. Mouth-broods its young — fascinating to watch.",
        ),
        FishItem(
            name = "Pajama Cardinal", type = "Saltwater", careLevel = "Easy", compatibility = 3,
            accent = AquariumColors.SoftLime, category = FishCategory.Peaceful,
            sizeCm = "8 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "5 years", diet = "Carnivore", origin = "Indo-Pacific",
            description = "Quirky-patterned schooling cardinalfish. Calm and reef-safe.",
        ),
        FishItem(
            name = "Damselfish", type = "Saltwater", careLevel = "Easy", compatibility = 1,
            accent = AquariumColors.LightAqua, category = FishCategory.Saltwater,
            sizeCm = "8–10 cm", tempC = "23–27 °C", phRange = "8.1–8.4",
            lifespanYears = "5–6 years", diet = "Omnivore", origin = "Indo-Pacific",
            description = "Hardy starter saltwater fish — but very territorial as it matures.",
        ),
    )

    val tanks = listOf(
        TankInfo("Coral Reef", 120, 14, "Saltwater", active = true),
        TankInfo("Living Room", 60, 8, "Freshwater", active = false),
        TankInfo("Office Nano", 25, 5, "Freshwater", active = false),
    )

    val devices = listOf(
        DeviceItem("Filter", "≋", true, "Filtering 320 L/h"),
        DeviceItem("Heater", "♨", true, "26.4°C target"),
        DeviceItem("Light", "☀", true, "Daylight cycle on"),
        DeviceItem("Air Pump", "✦", false, "Currently off"),
    )

    val equipment = listOf(
        EquipmentItem("Filter Cartridge", "Replace in 12 days", "▣"),
        EquipmentItem("Water Conditioner", "60% remaining", "✸"),
        EquipmentItem("Fish Food", "Stocked", "❖"),
        EquipmentItem("Test Kit", "Refill needed", "⌬"),
    )

    val maintenance = listOf(
        MaintenanceTask("Clean glass", initiallyDone = true),
        MaintenanceTask("Replace filter", initiallyDone = false),
        MaintenanceTask("Check heater", initiallyDone = true),
        MaintenanceTask("Add conditioner", initiallyDone = false),
    )

    val feeding = listOf(
        FeedingEntry("07:30", "Flakes", "2 pinches", done = true),
        FeedingEntry("13:00", "Pellets", "Small scoop", done = true),
        FeedingEntry("19:00", "Frozen brine", "Half cube", done = false),
        FeedingEntry("21:30", "Veggie wafer", "1 wafer", done = false),
    )

    val waterReadings = listOf(
        WaterReading("Temperature", "26.4", "°C", AquariumColors.Warning, listOf(25.7f, 26.1f, 26.5f, 26.3f, 26.4f, 26.6f, 26.4f)),
        WaterReading("pH", "7.6", "", AquariumColors.LightAqua, listOf(7.2f, 7.3f, 7.5f, 7.7f, 7.8f, 7.6f, 7.6f)),
        WaterReading("Ammonia", "0.02", "ppm", AquariumColors.Lime, listOf(0.04f, 0.03f, 0.03f, 0.02f, 0.02f, 0.01f, 0.02f)),
        WaterReading("Nitrate", "8.0", "ppm", AquariumColors.SoftLime, listOf(7.0f, 7.5f, 8.0f, 8.2f, 8.5f, 8.3f, 8.0f)),
        WaterReading("Nitrite", "0.00", "ppm", AquariumColors.PaleAqua, listOf(0.01f, 0.0f, 0.0f, 0.01f, 0.0f, 0.0f, 0.0f)),
    )
}
