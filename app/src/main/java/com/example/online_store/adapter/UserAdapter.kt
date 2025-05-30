package com.example.online_store.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.User
import com.example.online_store.utils.ImageUtils

class UserAdapter(
    private var users: List<User>,
    private val onEditClickListener: (User) -> Unit,
    private val onDeleteClickListener: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvUserRole: TextView = itemView.findViewById(R.id.tv_user_role)
        val ivEdit: ImageView = itemView.findViewById(R.id.iv_edit)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // Configurar los datos del usuario en la vista
        holder.tvUserName.text = user.name
        holder.tvUserEmail.text = user.email
        holder.tvUserRole.text = "Rol: ${if (user.role == User.ROLE_ADMIN) "Administrador" else "Usuario"}"

        // Configurar imagen de perfil
        loadUserProfileImage(user, holder.ivUserAvatar)

        // Configurar listeners para los botones de editar y eliminar
        holder.ivEdit.setOnClickListener {
            onEditClickListener(user)
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener(user)
        }
    }

    /**
     * Carga la imagen de perfil del usuario, priorizando imágenes personalizadas
     */
    private fun loadUserProfileImage(user: User, imageView: ImageView) {
        when {
            // Priorizar imagen personalizada (tomada con cámara/galería)
            !user.profilePicPath.isNullOrEmpty() && ImageUtils.imageFileExists(user.profilePicPath) -> {
                try {
                    val bitmap = BitmapFactory.decodeFile(user.profilePicPath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                        // Hacer la imagen circular (opcional)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        // Si falla la carga del archivo, usar imagen por defecto
                        imageView.setImageResource(R.drawable.ic_usuario)
                        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    imageView.setImageResource(R.drawable.ic_usuario)
                    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }
            // Si no tiene imagen personalizada, usar imagen por defecto
            else -> {
                imageView.setImageResource(R.drawable.ic_usuario)
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }

    override fun getItemCount(): Int = users.size

    // Método para actualizar la lista de usuarios
    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}