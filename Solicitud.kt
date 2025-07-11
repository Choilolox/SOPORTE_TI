dependencies {
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2"
    implementation "androidx.room:room-runtime:2.6.0"
    kapt "androidx.room:room-compiler:2.6.0"
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    implementation "com.google.firebase:firebase-messaging:23.1.2"
    implementation "com.google.android.material:material:1.9.0"
    implementation "androidx.recyclerview:recyclerview:1.3.1"
}

apply plugin: 'kotlin-kapt'
apply plugin: 'com.google.gms.google-services'

// ------------------ AndroidManifest.xml ------------------
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.empresa.soporte">

    <application
        android:allowBackup="true"
        android:label="Soporte Técnico"
        android:theme="@style/Theme.AppCompat.Light">

        <activity android:name=".ui.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>

// ------------------ Ticket.kt (Model) ------------------
data class Ticket(
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val estado: String = "Abierto",
    val fecha: String
)

// ------------------ TicketDao.kt ------------------
@Dao
interface TicketDao {
    @Query("SELECT * FROM ticket ORDER BY id DESC")
    fun getAll(): LiveData<List<Ticket>>

    @Insert
    suspend fun insert(ticket: Ticket)

    @Update
    suspend fun update(ticket: Ticket)

    @Delete
    suspend fun delete(ticket: Ticket)
}

// ------------------ AppDatabase.kt ------------------
@Database(entities = [Ticket::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}

// ------------------ TicketRepository.kt ------------------
class TicketRepository(private val dao: TicketDao) {
    val allTickets: LiveData<List<Ticket>> = dao.getAll()

    suspend fun insert(ticket: Ticket) = dao.insert(ticket)
    suspend fun update(ticket: Ticket) = dao.update(ticket)
    suspend fun delete(ticket: Ticket) = dao.delete(ticket)
}

// ------------------ TicketViewModel.kt ------------------
class TicketViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TicketRepository
    val allTickets: LiveData<List<Ticket>>

    init {
        val dao = Room.databaseBuilder(
            application,
            AppDatabase::class.java, "soporte_db"
        ).build().ticketDao()
        repository = TicketRepository(dao)
        allTickets = repository.allTickets
    }

    fun insert(ticket: Ticket) = viewModelScope.launch {
        repository.insert(ticket)
    }

    fun update(ticket: Ticket) = viewModelScope.launch {
        repository.update(ticket)
    }

    fun delete(ticket: Ticket) = viewModelScope.launch {
        repository.delete(ticket)
    }
}

// ------------------ MainActivity.kt ------------------
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: TicketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[TicketViewModel::class.java]

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val edtTitle = findViewById<EditText>(R.id.edtTitle)
        val edtDesc = findViewById<EditText>(R.id.edtDesc)
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = TicketAdapter()

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.allTickets.observe(this) {
            adapter.submitList(it)
        }

        btnAdd.setOnClickListener {
            val ticket = Ticket(
                titulo = edtTitle.text.toString(),
                descripcion = edtDesc.text.toString(),
                fecha = LocalDateTime.now().toString()
            )
            viewModel.insert(ticket)
            edtTitle.text.clear()
            edtDesc.text.clear()
        }
    }
}

// ------------------ TicketAdapter.kt ------------------
class TicketAdapter : ListAdapter<Ticket, TicketAdapter.TicketViewHolder>(DiffCallback()) {
    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtDesc: TextView = itemView.findViewById(R.id.txtDesc)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = getItem(position)
        holder.txtTitle.text = ticket.titulo
        holder.txtDesc.text = ticket.descripcion
        holder.txtEstado.text = ticket.estado
    }

    class DiffCallback : DiffUtil.ItemCallback<Ticket>() {
        override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket) = oldItem == newItem
    }
}

// ------------------ activity_main.xml ------------------
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/edtTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Título del ticket" />

    <EditText
        android:id="@+id/edtDesc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Descripción del problema" />

    <Button
        android:id="@+id/btnAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Registrar Ticket"
        android:layout_marginTop="8dp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="16dp" />
</LinearLayout>

// ------------------ item_ticket.xml ------------------
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="12dp">

    <TextView
        android:id="@+id/txtTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textStyle="bold"
        android:textSize="16sp" />

    <TextView
        android:id="@+id/txtDesc"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp" />

    <TextView
        android:id="@+id/txtEstado"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="#888888"
        android:textSize="12sp"
        android:layout_marginTop="4dp" />
</LinearLayout>
