package com.example.film.ui.activity.admin.fragment

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.film.database.FilmDTO
import com.example.film.databinding.FragmentAdminShowtimeManagementBinding
import com.example.film.model.ShowtimeModel
import com.example.film.repository.FilmRepository
import com.example.film.ui.adapter.ShowtimeManagementAdapter
import com.example.film.utils.Common
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowtimeManagementFragment : Fragment() {

    private var _binding: FragmentAdminShowtimeManagementBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val filmRepository = FilmRepository()
    private lateinit var showtimeAdapter: ShowtimeManagementAdapter
    private lateinit var movieSpinnerAdapter: ArrayAdapter<String>
    private val movieOptions = mutableListOf<FilmDTO>()
    private var showtimeListener: ListenerRegistration? = null
    private var editingShowtime: ShowtimeModel? = null
    private val selectedShowtimeCinemas = linkedSetOf<String>()
    private val selectedShowtimeDates = linkedSetOf<String>()
    private val selectedShowtimeTimes = linkedSetOf<String>()
    private var selectedFilterDate = ""
    private var selectedFilterCinema = ALL_CINEMAS_FILTER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminShowtimeManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupShowtimeRecyclerView()
        setupShowtimeForm()
        setupShowtimeFilters()
        listenToShowtimes()
        loadMovieOptions()

        binding.btnSaveShowtime.setOnClickListener { saveShowtime() }
        binding.btnCancelEditShowtime.setOnClickListener { clearShowtimeForm() }
    }

    private fun setupShowtimeRecyclerView() {
        showtimeAdapter = ShowtimeManagementAdapter(
            showtimes = emptyList(),
            onEdit = { showtime -> fillShowtimeForm(showtime) },
            onDelete = { showtime -> confirmDeleteShowtime(showtime) }
        )
        binding.rvShowtimeList.adapter = showtimeAdapter
    }

    private fun setupShowtimeForm() {
        movieSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            mutableListOf("Đang tải phim...")
        )
        movieSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerShowtimeMovie.adapter = movieSpinnerAdapter

        selectOnlyShowtimeCinema(Common.initCinema().first())
        selectOnlyShowtimeDate(adminDateOptions().first())
        selectOnlyShowtimeTime(Common.initEveningShowTimes().first())
        updateShowtimePickerLabels()
        renderShowtimeChips()

        binding.cardShowtimeCinema.setOnClickListener {
            if (editingShowtime != null) {
                showSelectionDialog(
                    title = "Chọn rạp chiếu",
                    options = Common.initCinema(),
                    selectedValue = selectedShowtimeCinemas.firstOrNull().orEmpty()
                ) { cinema ->
                    selectOnlyShowtimeCinema(cinema)
                    updateShowtimePickerLabels()
                    updateShowtimeActionLabel()
                }
            } else {
                showMultiSelectionDialog(
                    title = "Chọn rạp chiếu",
                    options = Common.initCinema(),
                    selectedValues = selectedShowtimeCinemas
                ) { cinemas ->
                    selectedShowtimeCinemas.clear()
                    selectedShowtimeCinemas.addAll(cinemas)
                    updateShowtimePickerLabels()
                    updateShowtimeActionLabel()
                }
            }
        }

        binding.cardShowtimeDate.setOnClickListener {
            if (editingShowtime != null) {
                showSelectionDialog(
                    title = "Chọn ngày chiếu",
                    options = adminDateOptions(),
                    selectedValue = selectedShowtimeDates.firstOrNull().orEmpty()
                ) { date ->
                    selectOnlyShowtimeDate(date)
                    updateShowtimePickerLabels()
                    updateShowtimeActionLabel()
                }
            } else {
                showMultiSelectionDialog(
                    title = "Chọn ngày chiếu",
                    options = adminDateOptions(),
                    selectedValues = selectedShowtimeDates
                ) { dates ->
                    selectedShowtimeDates.clear()
                    selectedShowtimeDates.addAll(dates)
                    updateShowtimePickerLabels()
                    updateShowtimeActionLabel()
                }
            }
        }
    }

    private fun setupShowtimeFilters() {
        selectedFilterDate = selectedShowtimeDates.firstOrNull() ?: adminDateOptions().first()
        selectedFilterCinema = ALL_CINEMAS_FILTER
        updateShowtimeFilterLabels()

        binding.cardFilterDate.setOnClickListener {
            showSelectionDialog(
                title = "Lọc theo ngày",
                options = adminDateOptions(),
                selectedValue = selectedFilterDate
            ) { date ->
                selectedFilterDate = date
                updateShowtimeFilterLabels()
                listenToShowtimes()
            }
        }

        binding.cardFilterCinema.setOnClickListener {
            showSelectionDialog(
                title = "Lọc theo rạp",
                options = listOf(ALL_CINEMAS_FILTER) + Common.initCinema(),
                selectedValue = selectedFilterCinema
            ) { cinema ->
                selectedFilterCinema = cinema
                updateShowtimeFilterLabels()
                listenToShowtimes()
            }
        }
    }

    private fun loadMovieOptions() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movies = withContext(Dispatchers.IO) {
                    (filmRepository.getNowPlayingMovies(1) + filmRepository.getPopularMovies(1))
                        .distinctBy { it.id }
                        .filter { !it.original_title.isNullOrBlank() }
                }

                if (_binding == null || !isAdded) return@launch

                movieOptions.clear()
                movieOptions.addAll(movies)
                movieSpinnerAdapter.clear()
                movieSpinnerAdapter.addAll(movies.map { it.original_title ?: "Không tên" })
                movieSpinnerAdapter.notifyDataSetChanged()
                editingShowtime?.let { selectMovieForShowtime(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_binding == null || !isAdded) return@launch
                movieSpinnerAdapter.clear()
                movieSpinnerAdapter.add("Không tải được phim")
                movieSpinnerAdapter.notifyDataSetChanged()
                showToast("Lỗi tải phim: ${e.message}")
            }
        }
    }

    private fun listenToShowtimes() {
        showtimeListener?.remove()

        val dateKeys = Common.dateKeyCandidates(selectedFilterDate)
        val query: Query = when (dateKeys.size) {
            0 -> db.collection("showtimes")
            1 -> db.collection("showtimes").whereEqualTo("dateKey", dateKeys.first())
            else -> db.collection("showtimes").whereIn("dateKey", dateKeys)
        }

        showtimeListener = query
            .addSnapshotListener { snapshot, e ->
                if (_binding == null || !isAdded) return@addSnapshotListener

                if (e != null) {
                    showToast("Lỗi tải suất chiếu")
                    showtimeAdapter.updateData(emptyList())
                    updateShowtimeListState(emptyList())
                    return@addSnapshotListener
                }

                val showtimes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ShowtimeModel::class.java)?.apply {
                        id = doc.id
                        if (showKey.isBlank()) showKey = doc.id
                    }
                }.orEmpty()
                    .filter { it.movieName.isNotBlank() && it.cinemaName.isNotBlank() && it.time.isNotBlank() }
                    .filter {
                        selectedFilterCinema == ALL_CINEMAS_FILTER ||
                                it.cinemaName == selectedFilterCinema
                    }
                    .sortedWith(
                        compareBy<ShowtimeModel> { it.dateKey }
                            .thenBy { it.cinemaName }
                            .thenBy { it.movieName }
                            .thenBy { parseStartHour(it.time) ?: Int.MAX_VALUE }
                    )

                showtimeAdapter.updateData(showtimes)
                updateShowtimeListState(showtimes)
            }
    }

    private fun saveShowtime() {
        val selectedMovie = movieOptions.getOrNull(binding.spinnerShowtimeMovie.selectedItemPosition)
        if (selectedMovie == null) {
            showToast("Chưa tải được danh sách phim")
            return
        }

        val movieName = selectedMovie.original_title?.trim().orEmpty()
        val cinemas = selectedShowtimeCinemas.toList()
        val dates = selectedShowtimeDates.toList()
        val times = selectedShowtimeTimes.toList()

        if (movieName.isBlank() || cinemas.isEmpty() || dates.isEmpty() || times.isEmpty()) {
            showToast("Vui lòng chọn đầy đủ phim, rạp, ngày và suất chiếu")
            return
        }

        val currentEditing = editingShowtime
        if (currentEditing != null) {
            val cinemaName = cinemas.first()
            val date = dates.first()
            saveSingleShowtime(selectedMovie, movieName, cinemaName, date, times.first(), currentEditing)
        } else {
            saveMultipleShowtimes(selectedMovie, movieName, cinemas, dates, times)
        }
    }

    private fun saveSingleShowtime(
        selectedMovie: FilmDTO,
        movieName: String,
        cinemaName: String,
        date: String,
        time: String,
        currentEditing: ShowtimeModel
    ) {
        val showKey = Common.buildShowKey(movieName, cinemaName, date, time)
        val oldShowKey = currentEditing.id
        val showtimeData = buildShowtimeData(
            selectedMovie = selectedMovie,
            movieName = movieName,
            cinemaName = cinemaName,
            date = date,
            time = time,
            currentEditing = currentEditing
        )

        db.collection("showtimes").document(showKey)
            .set(showtimeData, SetOptions.merge())
            .addOnSuccessListener {
                if (oldShowKey.isNotBlank() && oldShowKey != showKey) {
                    db.collection("showtimes").document(oldShowKey).delete()
                }
                showSavedSchedule(date, cinemaName)
                showToast("Đã lưu suất chiếu")
                if (_binding != null && isAdded) clearShowtimeForm(keepCurrentSelection = true)
            }
            .addOnFailureListener {
                showToast("Lỗi lưu suất chiếu")
            }
    }

    private fun saveMultipleShowtimes(
        selectedMovie: FilmDTO,
        movieName: String,
        cinemas: List<String>,
        dates: List<String>,
        times: List<String>
    ) {
        val drafts = dates.flatMap { date ->
            cinemas.flatMap { cinema ->
                times.map { time ->
                    ShowtimeDraft(cinemaName = cinema, date = date, time = time)
                }
            }
        }
        if (drafts.isEmpty()) {
            showToast("Vui lòng chọn đầy đủ phim, rạp, ngày và suất chiếu")
            return
        }

        commitShowtimeBatches(
            selectedMovie = selectedMovie,
            movieName = movieName,
            drafts = drafts,
            startIndex = 0
        )
    }

    private fun commitShowtimeBatches(
        selectedMovie: FilmDTO,
        movieName: String,
        drafts: List<ShowtimeDraft>,
        startIndex: Int
    ) {
        val batchDrafts = drafts.drop(startIndex).take(FIRESTORE_BATCH_LIMIT)
        val batch = db.batch()
        batchDrafts.forEach { draft ->
            val showKey = Common.buildShowKey(movieName, draft.cinemaName, draft.date, draft.time)
            val showtimeRef = db.collection("showtimes").document(showKey)
            batch.set(
                showtimeRef,
                buildShowtimeData(
                    selectedMovie = selectedMovie,
                    movieName = movieName,
                    cinemaName = draft.cinemaName,
                    date = draft.date,
                    time = draft.time,
                    currentEditing = null
                ),
                SetOptions.merge()
            )
        }

        batch.commit()
            .addOnSuccessListener {
                val nextIndex = startIndex + batchDrafts.size
                if (nextIndex < drafts.size) {
                    commitShowtimeBatches(
                        selectedMovie = selectedMovie,
                        movieName = movieName,
                        drafts = drafts,
                        startIndex = nextIndex
                    )
                    return@addOnSuccessListener
                }

                showSavedSchedule(drafts.first().date, drafts.first().cinemaName)
                val message = if (drafts.size == 1) "Đã thêm suất chiếu" else "Đã thêm ${drafts.size} suất chiếu"
                showToast(message)
                if (_binding != null && isAdded) clearShowtimeForm(keepCurrentSelection = true)
            }
            .addOnFailureListener {
                showToast("Lỗi lưu suất chiếu")
            }
    }

    private fun buildShowtimeData(
        selectedMovie: FilmDTO,
        movieName: String,
        cinemaName: String,
        date: String,
        time: String,
        currentEditing: ShowtimeModel?
    ): HashMap<String, Any> {
        val showKey = Common.buildShowKey(movieName, cinemaName, date, time)
        return hashMapOf<String, Any>(
            "id" to showKey,
            "showKey" to showKey,
            "movieId" to selectedMovie.id,
            "movieName" to movieName,
            "moviePoster" to (selectedMovie.poster_path ?: ""),
            "cinemaName" to cinemaName,
            "date" to date,
            "dateKey" to Common.extractDateKey(date),
            "time" to time,
            "active" to true,
            "createdAt" to (currentEditing?.createdAt ?: System.currentTimeMillis())
        ).apply {
            currentEditing?.let {
                put("bookedSeats", it.bookedSeats)
            }
        }
    }

    private fun showSavedSchedule(date: String, cinemaName: String) {
        selectedFilterDate = date
        selectedFilterCinema = cinemaName
        updateShowtimeFilterLabels()
        listenToShowtimes()
    }

    private fun fillShowtimeForm(showtime: ShowtimeModel) {
        editingShowtime = showtime
        selectMovieForShowtime(showtime)
        selectOnlyShowtimeCinema(showtime.cinemaName.ifBlank { Common.initCinema().first() })
        selectOnlyShowtimeDate(showtime.date.ifBlank { adminDateOptions().first() })
        selectOnlyShowtimeTime(showtime.time.ifBlank { Common.initEveningShowTimes().first() })
        updateShowtimePickerLabels()
        renderShowtimeChips()
        updateShowtimeActionLabel()
        binding.btnCancelEditShowtime.visibility = View.VISIBLE
    }

    private fun selectMovieForShowtime(showtime: ShowtimeModel) {
        val movieIndex = movieOptions.indexOfFirst { it.id == showtime.movieId || it.original_title == showtime.movieName }
        if (movieIndex >= 0) {
            binding.spinnerShowtimeMovie.setSelection(movieIndex)
            return
        }

        movieOptions.add(
            FilmDTO(
                id = showtime.movieId,
                poster_path = showtime.moviePoster,
                overview = null,
                vote_count = 0,
                original_title = showtime.movieName
            )
        )
        movieSpinnerAdapter.add(showtime.movieName)
        movieSpinnerAdapter.notifyDataSetChanged()
        binding.spinnerShowtimeMovie.setSelection(movieOptions.lastIndex)
    }

    private fun confirmDeleteShowtime(showtime: ShowtimeModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xoá suất chiếu")
            .setMessage("Xoá suất ${showtime.time} của ${showtime.movieName}?")
            .setNegativeButton("Huỷ", null)
            .setPositiveButton("Xoá") { _, _ ->
                db.collection("showtimes").document(showtime.id)
                    .delete()
                    .addOnSuccessListener {
                        showToast("Đã xoá suất chiếu")
                        if (_binding != null && isAdded && editingShowtime?.id == showtime.id) {
                            clearShowtimeForm()
                        }
                    }
                    .addOnFailureListener {
                        showToast("Lỗi xoá suất chiếu")
                    }
            }
            .show()
    }

    private fun clearShowtimeForm(keepCurrentSelection: Boolean = false) {
        editingShowtime = null
        if (!keepCurrentSelection && movieOptions.isNotEmpty()) {
            binding.spinnerShowtimeMovie.setSelection(0)
        }
        if (!keepCurrentSelection) {
            selectOnlyShowtimeCinema(Common.initCinema().first())
            selectOnlyShowtimeDate(adminDateOptions().first())
            selectOnlyShowtimeTime(Common.initEveningShowTimes().first())
        } else if (selectedShowtimeTimes.isEmpty()) {
            selectOnlyShowtimeTime(Common.initEveningShowTimes().first())
        }
        updateShowtimePickerLabels()
        renderShowtimeChips()
        updateShowtimeActionLabel()
        binding.btnCancelEditShowtime.visibility = View.GONE
    }

    private fun updateShowtimePickerLabels() {
        binding.txtSelectedShowtimeCinema.text = summarizeSelection(
            values = selectedShowtimeCinemas,
            emptyText = "Chọn rạp",
            manySuffix = "rạp đã chọn"
        )
        binding.txtSelectedShowtimeDate.text = summarizeSelection(
            values = selectedShowtimeDates,
            emptyText = "Chọn ngày",
            manySuffix = "ngày đã chọn"
        )
    }

    private fun updateShowtimeFilterLabels() {
        binding.txtFilterDate.text = selectedFilterDate
        binding.txtFilterCinema.text = selectedFilterCinema
    }

    private fun updateShowtimeListState(showtimes: List<ShowtimeModel>) {
        binding.txtShowtimeListTitle.text = "Danh sách suất chiếu (${showtimes.size})"
        binding.txtShowtimeEmpty.visibility = if (showtimes.isEmpty()) View.VISIBLE else View.GONE
        binding.rvShowtimeList.visibility = if (showtimes.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun renderShowtimeChips() {
        val times = (Common.initEveningShowTimes() + selectedShowtimeTimes)
            .filter { it.isNotBlank() }
            .distinct()

        binding.gridShowtimeTimes.removeAllViews()
        times.forEach { time ->
            val checked = selectedShowtimeTimes.contains(time)
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = time
                gravity = Gravity.CENTER
                isCheckable = true
                isChecked = checked
                chipBackgroundColor = ColorStateList.valueOf(
                    Color.parseColor(if (checked) "#3B82F6" else "#334155")
                )
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#475569"))
                chipStrokeWidth = dp(1).toFloat()
                setTextColor(Color.WHITE)
                textSize = 13f
                setOnClickListener {
                    toggleShowtimeTime(time)
                    renderShowtimeChips()
                }
            }
            chip.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(3), dp(3), dp(3), dp(7))
            }
            binding.gridShowtimeTimes.addView(chip)
        }
        updateShowtimeActionLabel()
    }

    private fun toggleShowtimeTime(time: String) {
        if (editingShowtime != null) {
            selectOnlyShowtimeTime(time)
            return
        }

        if (selectedShowtimeTimes.contains(time)) {
            selectedShowtimeTimes.remove(time)
        } else {
            selectedShowtimeTimes.add(time)
        }
    }

    private fun selectOnlyShowtimeTime(time: String) {
        selectedShowtimeTimes.clear()
        if (time.isNotBlank()) {
            selectedShowtimeTimes.add(time)
        }
    }

    private fun selectOnlyShowtimeCinema(cinema: String) {
        selectedShowtimeCinemas.clear()
        if (cinema.isNotBlank()) {
            selectedShowtimeCinemas.add(cinema)
        }
    }

    private fun selectOnlyShowtimeDate(date: String) {
        selectedShowtimeDates.clear()
        if (date.isNotBlank()) {
            selectedShowtimeDates.add(date)
        }
    }

    private fun updateShowtimeActionLabel() {
        val total = selectedShowtimeCinemas.size * selectedShowtimeDates.size * selectedShowtimeTimes.size
        binding.btnSaveShowtime.text = when {
            editingShowtime != null -> "CẬP NHẬT SUẤT CHIẾU"
            total == 0 -> "CHỌN SUẤT CHIẾU"
            total == 1 -> "THÊM SUẤT CHIẾU"
            else -> "THÊM $total SUẤT CHIẾU"
        }
    }

    private fun summarizeSelection(
        values: Set<String>,
        emptyText: String,
        manySuffix: String
    ): String {
        return when (values.size) {
            0 -> emptyText
            1 -> values.first()
            else -> "${values.size} $manySuffix"
        }
    }

    private fun showSelectionDialog(
        title: String,
        options: List<String>,
        selectedValue: String,
        onSelected: (String) -> Unit
    ) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
            setBackgroundColor(Color.parseColor("#0F172A"))
        }
        val titleView = TextView(requireContext()).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(12))
        }
        val listContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        options.forEach { option ->
            val selected = option == selectedValue
            val card = MaterialCardView(requireContext()).apply {
                radius = dp(10).toFloat()
                strokeWidth = dp(1)
                strokeColor = Color.parseColor(if (selected) "#3B82F6" else "#334155")
                setCardBackgroundColor(Color.parseColor(if (selected) "#1E3A8A" else "#1E293B"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
                setOnClickListener {
                    onSelected(option)
                    dialog.dismiss()
                }
            }
            val label = TextView(requireContext()).apply {
                text = option
                setTextColor(Color.WHITE)
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
            card.addView(label)
            listContainer.addView(card)
        }

        val scrollView = NestedScrollView(requireContext()).apply {
            addView(listContainer)
        }
        container.addView(titleView)
        container.addView(scrollView)
        dialog.setView(container)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

    private fun showMultiSelectionDialog(
        title: String,
        options: List<String>,
        selectedValues: Set<String>,
        onSelected: (List<String>) -> Unit
    ) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val workingSelection = selectedValues.toMutableSet()
        val rowViews = mutableListOf<Pair<MaterialCardView, TextView>>()
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
            setBackgroundColor(Color.parseColor("#0F172A"))
        }
        val titleView = TextView(requireContext()).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(12))
        }
        val listContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun refreshRows() {
            rowViews.forEach { (card, label) ->
                val option = label.tag as String
                val selected = workingSelection.contains(option)
                card.strokeColor = Color.parseColor(if (selected) "#3B82F6" else "#334155")
                card.setCardBackgroundColor(Color.parseColor(if (selected) "#1E3A8A" else "#1E293B"))
                label.text = if (selected) "✓ $option" else option
            }
        }

        options.forEach { option ->
            val card = MaterialCardView(requireContext()).apply {
                radius = dp(10).toFloat()
                strokeWidth = dp(1)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            }
            val label = TextView(requireContext()).apply {
                tag = option
                setTextColor(Color.WHITE)
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
            card.setOnClickListener {
                if (workingSelection.contains(option)) {
                    workingSelection.remove(option)
                } else {
                    workingSelection.add(option)
                }
                refreshRows()
            }
            card.addView(label)
            rowViews.add(card to label)
            listContainer.addView(card)
        }

        val actionRow = LinearLayout(requireContext()).apply {
            gravity = Gravity.END
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }
        val cancelButton = TextView(requireContext()).apply {
            text = "Huỷ"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 15f
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setOnClickListener { dialog.dismiss() }
        }
        val doneButton = TextView(requireContext()).apply {
            text = "Xong"
            setTextColor(Color.parseColor("#3B82F6"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(10), 0, dp(10))
            setOnClickListener {
                if (workingSelection.isEmpty()) {
                    showToast("Vui lòng chọn ít nhất 1 mục")
                    return@setOnClickListener
                }
                onSelected(options.filter { workingSelection.contains(it) })
                dialog.dismiss()
            }
        }
        actionRow.addView(cancelButton)
        actionRow.addView(doneButton)

        val scrollView = NestedScrollView(requireContext()).apply {
            addView(listContainer)
        }
        container.addView(titleView)
        container.addView(scrollView)
        container.addView(actionRow)
        dialog.setView(container)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            refreshRows()
        }
        dialog.show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun parseStartHour(showtime: String): Int? {
        return showtime.substringBefore(" - ").substringBefore(":").trim().toIntOrNull()
    }

    private fun showToast(message: String) {
        context?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    private fun adminDateOptions(): List<String> {
        return Common.initDate(daysAhead = 30)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showtimeListener?.remove()
        showtimeListener = null
        _binding = null
    }

    companion object {
        private const val ALL_CINEMAS_FILTER = "Tất cả rạp"
        private const val FIRESTORE_BATCH_LIMIT = 450
    }
}

private data class ShowtimeDraft(
    val cinemaName: String,
    val date: String,
    val time: String
)
