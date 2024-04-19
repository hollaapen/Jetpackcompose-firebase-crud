package net.ezra.ui.students

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.ezra.navigation.ROUTE_ADD_STUDENTS
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_VIEW_STUDENTS


data class Item(

    val imageUrl: String? = "",
    val studentName: String? = "",
    val studentClass: String? = "",
    val phone: String? = "",
    val location: String? = ""


)


class FirestoreViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val itemsCollection = firestore.collection("Students")

    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    init {
        fetchItems()
    }

    fun fetchItems() {
        itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreViewModel", "Error fetching items", error)
                return@addSnapshotListener
            }

            val itemList = mutableListOf<Item>()
            snapshot?.documents?.forEach { document ->
                val item = document.toObject(Item::class.java)
                item?.let {
                    itemList.add(it)
                }
            }
            _items.value = itemList
        }
    }
}


@Composable
fun ItemList(items: List<Item>) {

    Column {



        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            items.forEach { item ->
                item {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){

                           SubcomposeAsyncImage(
                               model = ImageRequest.Builder(LocalContext.current)
                                   .data(item.imageUrl)
                                   .crossfade(true)
                                   .build(),
                               loading = {
                                   CircularProgressIndicator()
                               },
                               contentDescription = item.studentName,
                               modifier = Modifier
                                   .clip(RoundedCornerShape(10))
                                   .size(150.dp)

                           )
                        item.studentName?.let { Text(text = it) }
                        item.phone?.let { Text(text = it) }



                    }

                }
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Students(navController: NavHostController, viewModel: FirestoreViewModel) {
    val items by viewModel.items.observeAsState(initial = emptyList())

    // Fetch items when the composable is first created
    LaunchedEffect(viewModel) {
        viewModel.fetchItems()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Students")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_VIEW_STUDENTS) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xff0FB06A),
                    titleContentColor = Color.White,
                ),
            )
        }, content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color(0xff9AEDC9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ItemList(items)


            }
        },

        )
    


    
}