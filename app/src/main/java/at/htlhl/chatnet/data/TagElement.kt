package at.htlhl.chatnet.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Snowboarding
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class TagElement(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val tags = listOf(
    TagElement("Music", Icons.Default.Headphones, Color(0xFFFF0000)),
    TagElement("Movies", Icons.Default.LocalMovies, Color(0xFFFF0000)),
    TagElement("Reading", Icons.Default.MenuBook, Color(0xFFFF0000)),
    TagElement("Drawing", Icons.Default.Draw, Color(0xFFFF0000)),
    TagElement("Gaming", Icons.Default.VideogameAsset, Color(0xFFFF0000)),
    TagElement("Coding", Icons.Default.Code, Color(0xFFFF0000)),
    TagElement("Singing", Icons.Default.MusicNote, Color(0xFFFF0000)),
    TagElement("Dancing", Icons.Default.Details, Color(0xFFFF0000)),
    TagElement("Cinema", Icons.Default.Movie, Color(0xFFFF0000)),
    TagElement("Dining Out", Icons.Default.Dining, Color(0xFFFF0000)),
    TagElement("Walking", Icons.Default.DirectionsWalk, Color(0xFF001AFF)),
    TagElement("Running", Icons.Default.DirectionsRun, Color(0xFF001AFF)),
    TagElement("Hiking", Icons.Default.Hiking, Color(0xFF001AFF)),
    TagElement("Cycling", Icons.Default.DirectionsBike, Color(0xFF001AFF)),
    TagElement("Swimming", Icons.Default.Water, Color(0xFF001AFF)),
    TagElement("Training", Icons.Default.SportsGymnastics, Color(0xFF001AFF)),
    TagElement("Skiing", Icons.Default.DownhillSkiing, Color(0xFF001AFF)),
    TagElement("Snowboarding", Icons.Default.Snowboarding, Color(0xFF001AFF)),
    TagElement("Skateboarding", Icons.Default.Skateboarding, Color(0xFF001AFF)),
    TagElement("Soccer", Icons.Default.SportsSoccer, Color(0xFF001AFF)),
    TagElement("Football", Icons.Default.SportsFootball, Color(0xFF001AFF)),
    TagElement("Basketball", Icons.Default.SportsBasketball, Color(0xFF001AFF)),
    TagElement("Tennis", Icons.Default.SportsTennis, Color(0xFF001AFF)),
    TagElement("Student", Icons.Default.School, Color(0xFF00FF21)),
    TagElement("Entrepreneur", Icons.Default.Engineering, Color(0xFF00FF21)),
    TagElement("Travelling", Icons.Default.CardTravel, Color(0xFF00FF21)),
    TagElement("Shopping", Icons.Default.ShoppingCart, Color(0xFF00FF21)),
    TagElement("Cooking", Icons.Default.Kitchen, Color(0xFF00FF21)),
    TagElement("Fashion", Icons.Default.Details, Color(0xFF00FF21)),
    TagElement("Vegan", Icons.Default.FoodBank, Color(0xFF00FF21)),
    TagElement("LGBTQ+", Icons.Default.Flag, Color(0xFF00FF21)),
    TagElement("Cars", Icons.Default.DirectionsCar, Color(0xFFFF5722)),
    TagElement("Technology", Icons.Default.Computer, Color(0xFFFF5722)),
    TagElement("Social Media", Icons.Default.Smartphone, Color(0xFFFF5722)),
    TagElement("Dogs", Icons.Default.Pets, Color(0xFF5C3326)),
    TagElement("Cats", Icons.Default.Pets, Color(0xFF5C3326)),
    TagElement("No Tags", Icons.Default.RemoveCircleOutline, Color.Gray),
)
