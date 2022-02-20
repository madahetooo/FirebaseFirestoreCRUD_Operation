package com.apps.firebaseauthfulldemo2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val personalCollectionRef = Firebase.firestore.collection("person")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnUploadData.setOnClickListener {
            val firstname = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()
            var person = Person(firstname, lastName, age)
            savePerson(person)
        }
        btnDeleteData.setOnClickListener {
            val oldData = getOldPerson()
            deletePerson(oldData)
        }
        btnRetrieveData.setOnClickListener {
            retrievePersons()
        }
//        btnUpdate.setOnClickListener {
//            val oldPerson = getOldPerson()
//            val newPerson = getNewPersonMap()
//            updatePerson(oldPerson,newPerson)
//        }
//        subscribeToRealTimeUpdates()

    }
    private fun getOldPerson() :Person{
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val age = etAge.text.toString().toInt()
        return Person(firstName,lastName,age)
    }
    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = etNewFirstName.text.toString()
        val lastName = etNewLastName.text.toString()
        val age = etNewAge.text.toString()
        val map = mutableMapOf<String, Any>()
        if(firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if(lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if(age.isNotEmpty()) {
            map["age"] = age.toInt()
        }
        return map
    }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {

        val queryPerson = personalCollectionRef
            .whereEqualTo("firstName",person.firstName)
            .whereEqualTo("lastName",person.lastName)
            .whereEqualTo("age",person.age)
            .get()
            .await()
        if (queryPerson.documents.isNotEmpty()){
            for (document in queryPerson){
                try {
                    personalCollectionRef.document(document.id).delete().await()

                }catch (e:Exception){
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }

    private fun subscribeToRealTimeUpdates() {
        personalCollectionRef.addSnapshotListener { querySnapShot, FirebaseFireStoreException ->
            FirebaseFireStoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapShot?.let {
                val stringBuilder = StringBuilder()
                for (document in querySnapShot) {
                    val person = document.toObject<Person>()
                    stringBuilder.append("$person\n")
                }
                tvShowData.text = stringBuilder.toString()
            }

        }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = etFromAge.text.toString().toInt()
        val toAge = etToAge.text.toString().toInt()
        try {
            val querySnapshot = personalCollectionRef
                .whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                .get()
                .await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<Person>()
                sb.append("$person\n")
            }
            withContext(Dispatchers.Main) {
                tvShowData.text = sb.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePerson(person: Person) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                personalCollectionRef.add(person).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Successfully Uploaded Data",
                        Toast.LENGTH_LONG
                    ).show()

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()

                }
            }
        }
    }
}


//private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
//    val personQuery = personCollectionRef
//        .whereEqualTo("firstName", person.firstName)
//        .whereEqualTo("lastName", person.lastName)
//        .whereEqualTo("age", person.age)
//        .get()
//        .await()
//    if(personQuery.documents.isNotEmpty()) {
//        for(document in personQuery) {
//            try {
//                //personCollectionRef.document(document.id).update("age", newAge).await()
//                personCollectionRef.document(document.id).set(
//                    newPersonMap,
//                    SetOptions.merge()
//                ).await()
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    } else {
//        withContext(Dispatchers.Main) {
//            Toast.makeText(this@MainActivity, "No persons matched the query.", Toast.LENGTH_LONG).show()
//        }
//    }
//}