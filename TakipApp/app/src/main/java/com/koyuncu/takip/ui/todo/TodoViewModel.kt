package com.koyuncu.takip.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koyuncu.takip.data.local.TodoEntity
import com.koyuncu.takip.data.repo.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repo: TodoRepository) : ViewModel() {
    val todos: StateFlow<List<TodoEntity>> =
        repo.todos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(title: String) = viewModelScope.launch { repo.add(title) }
    fun toggle(todo: TodoEntity) = viewModelScope.launch { repo.toggle(todo) }
    fun delete(todo: TodoEntity) = viewModelScope.launch { repo.delete(todo) }
}
