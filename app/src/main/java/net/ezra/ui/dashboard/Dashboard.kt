package net.ezra.ui.dashboard



import android.app.ProgressDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import net.ezra.navigation.ROUTE_LOGIN

import com.google.firebase.firestore.SetOptions


private var progressDialog: ProgressDialog? = null
@Composable
fun DashboardScreen(navController: NavHostController)  {
    var school by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var user: User? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current




    // Fetch user details from Firestore
    LaunchedEffect(key1 = currentUser?.uid) {
        if (currentUser != null) {
            val userDocRef = firestore.collection("users").document(currentUser.uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        user = document.toObject<User>()
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("User Details", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        // Display the email of the logged-in user
        Text("Email: ${currentUser?.email ?: "N/A"}")

        if (isLoading) {
            // Show loading indicator
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            // Display user details
            user?.let {
                Text("School: ${it.school}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${it.name}")
                Spacer(modifier = Modifier.height(8.dp))

                // Add more user details here
            }
        }


        Spacer(modifier = Modifier.height(15.dp))

       Text(text = "Update profile")
        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            label = { Text("School") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Add a TextField for entering name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Add a button to submit the details
        Button(
            onClick = {

               progressDialog = ProgressDialog(context)
                progressDialog?.setMessage("Updating profile...")
                progressDialog?.setCancelable(false)
                progressDialog?.show()

                val user = User(currentUser!!.uid, school, name)

                saveUserDetails(user) {
                    // Handle success or failure
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(ROUTE_LOGIN)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }





    }
}

data class User(
    val userId: String = "",
    val school: String = "",
    val name: String = ""
)

fun saveUserDetails(user: User, param: (Any) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").document(user.userId)
        .set(user, SetOptions.merge())
        .addOnSuccessListener {

            progressDialog?.dismiss()
            // Success message or navigation
        }
        .addOnFailureListener {

            progressDialog?.dismiss()
            // Handle failure
        }
}
