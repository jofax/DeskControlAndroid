package com.smartpods.android.pulseecho.Utilities
import io.realm.DynamicRealm
import io.realm.RealmMigration

open class DBRealmMigration:RealmMigration {
    private lateinit var _models:Array<Class<*>>
    fun RealmMigrations(models:Array<Class<*>>):Array<Class<*>>{
        _models = models
        return _models
    }
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        for (classModel in _models)
        {
            val userSchema = schema.get(classModel.getSimpleName())
            val fields = classModel.getDeclaredFields()
            val fieldsDatabase = userSchema?.getFieldNames()
            if (fieldsDatabase != null) {
                for (fieldDatabase in fieldsDatabase) {
                    var removed = true
                    for (field in fields) {
                        if (field.name.compareTo(fieldDatabase) === 0) {
                            removed = false
                            break
                        }
                    }
                    if (removed) {
                        if (userSchema != null) {
                            userSchema.removeField(fieldDatabase)
                        }
                    }
                }
            }
            for (field in fields)
            {
                val type = field.type
                val name = field.name
                if (userSchema != null) {
                    if (!userSchema.hasField(name)) {
                        userSchema.addField(name, GetClassOfType(type))
                    }
                }
            }
        }
    }
    private fun GetClassOfType(type:Class<*>): Class<Int>? {
        val name = type.simpleName
        if (name.compareTo("string", ignoreCase = true) == 0)
        {
            return String::class.java as Class<Int>?
        }
        else if (name.compareTo("int", ignoreCase = true) == 0)
        {
            return Int::class.javaPrimitiveType
        }
        else if (name.compareTo("boolean", ignoreCase = true) == 0)
        {
            return Boolean::class.java as Class<Int>?
        }
        else if (name.compareTo("byte", ignoreCase = true) == 0)
        {
            return Byte::class.javaPrimitiveType as Class<Int>?
        }
        else if (name.compareTo("byte[]", ignoreCase = true) == 0)
        {
            return ByteArray::class.java as Class<Int>?
        }
        return Boolean::class.java as Class<Int>?
    }
}