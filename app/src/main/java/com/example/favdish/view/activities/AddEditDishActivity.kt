package com.example.favdish.view.activities

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.base.BaseActivity
import com.example.favdish.databinding.ActivityAddEditDishBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.DialogCustomSelectionBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.ListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@Suppress("DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
class AddEditDishActivity : View.OnClickListener, BaseActivity<ActivityAddEditDishBinding>(
    ActivityAddEditDishBinding::inflate
) {
    private lateinit var addCustomListDialog: Dialog
    private var dishDetails: FavDish? = null
    private lateinit var imageUri : Uri
    private var imageStoragePath: String = ""
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as App).repository)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (!permissions.containsValue(false)) {
                startCamera()
            } else {
                Toast.makeText(this, "Unable to use this feature, please allow permission(s).", Toast.LENGTH_SHORT).show()
                Timber.tag("Permissions").d("Permissions denied.")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode === RESULT_OK) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }
                binding.ivDishImage.setImageBitmap(bitmap)

                imageStoragePath = saveImageToInternalStorage(bitmap)

                binding.ivAddDishImage.setImageDrawable(
                    ContextCompat.getDrawable(this@AddEditDishActivity, R.drawable.ic_edit))
            } else {
                Toast.makeText(this, "Nothing was selected.", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(this.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                binding.ivDishImage.setImageBitmap(bitmap)

                imageStoragePath = saveImageToInternalStorage(bitmap)

                binding.ivAddDishImage.setImageDrawable(
                    ContextCompat.getDrawable(this@AddEditDishActivity, R.drawable.ic_edit))
            } else {
                Toast.makeText(this, "Nothing was selected.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            dishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setUpActionBar()

        dishDetails?.let {
            if (it.id != 0) {
                imageStoragePath = it.image
                binding.ivDishImage.setImageDrawable(Drawable.createFromPath(imageStoragePath))
                binding.etTitle.setText(it.title)
                binding.etType.setText(it.type)
                binding.etCategory.setText(it.category)
                binding.etIngredients.setText(it.ingredients)
                binding.etCookingTime.setText(it.cookingTime)
                binding.etDirectionToCook.setText(it.directionToCook)

                binding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }
        binding.ivAddDishImage.setOnClickListener(this@AddEditDishActivity)
        binding.etType.setOnClickListener(this@AddEditDishActivity)
        binding.etCategory.setOnClickListener(this@AddEditDishActivity)
        binding.etCookingTime.setOnClickListener(this@AddEditDishActivity)
        binding.btnAddDish.setOnClickListener(this@AddEditDishActivity)
    }

    override fun onClick(view: View) {
            when (view.id) {
                binding.ivAddDishImage.id -> {
                    customSelectionDialog()
                    return
                }

                binding.etType.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }

                binding.etCategory.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }

                binding.etCookingTime.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }

                binding.btnAddDish.id -> {
                    val title = binding.etTitle.text.toString().trim { it <= ' ' }
                    val type = binding.etType.text.toString().trim { it <= ' ' }
                    val category = binding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients =
                        binding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        binding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {
                        TextUtils.isEmpty(imageStoragePath) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            var id = 0
                            var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                            var favoriteCheck = false

                            dishDetails?.let {
                                if (it.id != 0) {
                                    id = it.id
                                    imageSource = it.imageSource
                                    favoriteCheck = it.favoriteDish
                                }
                            }

                            val favDishDetails = FavDish(
                                imageStoragePath,
                                imageSource,
                                title, type, category, ingredients, cookingTimeInMinutes,
                                cookingDirection, favoriteCheck, id
                            )

                            if (id == 0) {
                                favDishViewModel.insert(favDishDetails)
                                Toast.makeText(this,"Dish added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                favDishViewModel.update(favDishDetails)
                                Toast.makeText(this,"Updated successfully", Toast.LENGTH_SHORT).show()
                            }

                            finish()
                        }
                    }
                }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.tbrAddDish)

        if (dishDetails != null && dishDetails!!.id != 0) {
            supportActionBar?.let { it.title = resources.getString(R.string.title_edit_dish) }
        } else {
            supportActionBar?.let { it.title = resources.getString(R.string.title_add_dish) }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding.tbrAddDish.setNavigationOnClickListener{onBackPressedDispatcher.onBackPressed()}
    }

    private fun customSelectionDialog() {
        val dialog = Dialog(this@AddEditDishActivity)

        val binding: DialogCustomSelectionBinding = DialogCustomSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.show()

        binding.tvCamera.setOnClickListener {
            val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionLauncher.launch(cameraPermissions)
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }
    }

    private fun startCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(cameraIntent)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("FavDishImages", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) { e.printStackTrace() }
        return file.absolutePath
    }

    //done
    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        addCustomListDialog = Dialog(this@AddEditDishActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        addCustomListDialog.setContentView(binding.root)

        binding.tvDialogTitle.text = title
        binding.rvDialogList.layoutManager = LinearLayoutManager(this@AddEditDishActivity)
        val adapter = ListItemAdapter(this@AddEditDishActivity, null, itemsList, selection)
        binding.rvDialogList.adapter = adapter

        addCustomListDialog.show()
    }

    //done
    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                addCustomListDialog.dismiss()
                binding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                addCustomListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            else -> {
                addCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }
}