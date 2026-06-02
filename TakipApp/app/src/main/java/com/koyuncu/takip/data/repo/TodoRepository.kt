package com.koyuncu.takip.data.repo

import com.koyuncu.takip.data.local.TodoDao
import com.koyuncu.takip.data.local.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val dao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = dao.observeAll()

    suspend fun add(title: String) {
        val t = title.trim()
        if (t.isNotEmpty()) dao.insert(TodoEntity(title = t))
    }

    suspend fun toggle(todo: TodoEntity) = dao.update(todo.copy(done = !todo.done))
    suspend fun delete(todo: TodoEntity) = dao.delete(todo)
}
