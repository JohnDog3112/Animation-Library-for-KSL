class Tokens(str: String) {
    private val tokens: List<String> = str.split(" ");
    private var index: Int = 0;

    fun next(): String {
        return tokens[index++];
    }
    fun peek(offset: Int = 0): String {
        return tokens[index + offset];
    }
    fun assertNext(checkVal: String) {
        val n = next();
        if (n != checkVal) {
            error("Expected $checkVal got $n");
        }
    }
    fun checkNext(checkVal: String): Boolean {
        if (peek() == checkVal) {
            next();
            return true;
        } else {
            return false;
        }
    }
    fun assertPeek(checkVal: String, offset: Int = 0) {
        val n = peek(offset);
        if (n != checkVal) {
            error("Expected $checkVal got $n");
        }
    }
    fun checkPeek(checkVal: String, offset: Int = 0): Boolean {
        if (peek(offset) == checkVal) {
            return true;
        } else {
            return false;
        }
    }
}

sealed class LoggedObject() {
    data class Entity(val type: String, val id: String) : LoggedObject() {
        companion object {
            fun fromString(tokens: Tokens): Entity {
                tokens.assertNext("Entity");
                val type = tokens.next();
                val id = tokens.next();

                return Entity(type, id);
            }
        }
        override fun toString(): String {
            return "Entity $type $id";
        }
    }
    data class Unique(val id: String) : LoggedObject() {
        companion object {
            fun fromString(tokens: Tokens): Unique {
                val id = tokens.next();

                return Unique(id);
            }
        }
        override fun toString(): String {
            return "$id";
        }
    }

    companion object {
        fun fromString(tokens: Tokens): LoggedObject {
            return if (tokens.peek() == "Entity") {
                Entity.fromString(tokens);
            } else {
                Unique.fromString(tokens)
            }
        }
    }
}

fun parseTime(tokens: Tokens): Long {
    tokens.assertNext("at");
    tokens.assertNext("tick");
    val time = tokens.next().toLong();
    return time;
}
sealed class LogCommand(open val time: Long) {
    sealed class Change(override open val time: Long) : LogCommand(time)  {
        data class Variable(val id: String, val column: Int?, val row: Int?, val value: Int, override val time: Long) : Change(time) {
            companion object {
                fun fromString(tokens: Tokens): Variable {
                    tokens.assertNext("Change");
                    tokens.assertNext("Variable");
                    val id = tokens.next();
                    var column: Int? = null;
                    var row: Int? = null;
                    if (tokens.checkNext("at")) {
                        column = tokens.next().toInt();
                        row = tokens.next().toInt();
                    }
                    tokens.assertNext("to");
                    val value = tokens.next().toInt();
                    val time = parseTime(tokens);

                    return Variable(id, column, row, value, time);
                }
            }
            override fun toString(): String {
                if (column != null) {
                    if (row == null) error("This shouldn't be possible!");
                    return "Change Variable $id at $column $row to $value at tick $time";
                } else {
                    return "Change Variable $id to $value at tick $time";
                }
            }
        }
        data class Image(val obj: LoggedObject, val imageID: String, override val time: Long) : Change(time) {
            companion object {
                fun fromString(tokens: Tokens): Image {
                    tokens.assertNext("Change");
                    tokens.assertNext("Image");
                    val obj = LoggedObject.fromString(tokens);
                    tokens.assertNext("to");
                    val imageID = tokens.next();
                    val time = parseTime(tokens);

                    return Image(obj, imageID, time);
                }
            }
            override fun toString(): String {
                return "Change Image ${obj.toString()} to $imageID at tick $time";
            }
        }

        companion object {
            fun fromString(tokens: Tokens): Change {
                tokens.assertPeek("Change");
                return if (tokens.checkPeek("Variable", 1)) {
                    return Variable.fromString(tokens);
                } else {
                    return Image.fromString(tokens);
                }
            }
        }
    }
    
    data class PushQueueStack(val entity: LoggedObject.Entity, val queueStackID: String, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): PushQueueStack {
                tokens.assertNext("Push");
                val entity = LoggedObject.Entity.fromString(tokens);
                tokens.assertNext("onto");
                val queueStackID = tokens.next();
                val time = parseTime(tokens);
                
                return PushQueueStack(entity, queueStackID, time);
            }
        }
        override fun toString(): String {
            return "Push ${entity.toString()} onto $queueStackID at tick $time";
        }
    }
    data class PopQueueStack(val entity: LoggedObject.Entity, val queueStackID: String, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): PopQueueStack {
                tokens.assertNext("Pop");
                val entity = LoggedObject.Entity.fromString(tokens);
                tokens.assertNext("off");
                tokens.assertNext("of");
                val queueStackID = tokens.next();
                val time = parseTime(tokens);
                
                return PopQueueStack(entity, queueStackID, time);
            }
        }
        override fun toString(): String {
            return "Pop ${entity.toString()} off of $queueStackID at tick $time";
        }
    }
    data class AddResource(val resource: String, val targetID: String, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): AddResource {
                tokens.assertNext("Add");
                tokens.assertNext("Resource");
                val resource = tokens.next();
                tokens.assertNext("to");
                val targetID = tokens.next();
                val time = parseTime(tokens);

                return AddResource(resource, targetID, time);
            }
        }
        override fun toString(): String {
            return "Add Resource $resource to $targetID at tick $time";
        }
    }
    data class PickupEntity(val entity: LoggedObject.Entity, val transporterID: String, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): PickupEntity {
                tokens.assertNext("Pickup");
                val entity = LoggedObject.Entity.fromString(tokens);
                tokens.assertNext("onto");
                val transporterID = tokens.next();
                val time = parseTime(tokens);

                return PickupEntity(entity, transporterID, time);
            }
        }
        override fun toString(): String {
            return "Pickup ${entity.toString()} onto $transporterID at tick $time";
        }
    }
    data class DropoffEntity(val entity: LoggedObject.Entity, val transporterID: String, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): DropoffEntity {
                tokens.assertNext("Dropoff");
                val entity = LoggedObject.Entity.fromString(tokens);
                tokens.assertNext("from");
                val transporterID = tokens.next();
                val time = parseTime(tokens);

                return DropoffEntity(entity, transporterID, time);
            }
        }
        override fun toString(): String {
            return "Dropoff ${entity.toString()} from $transporterID at tick $time";
        }
    }
    data class Move(val movedObject: LoggedObject, val location: String, val duration: Long, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): Move {
                tokens.assertNext("Move");
                val obj = LoggedObject.fromString(tokens);
                tokens.assertNext("to");
                val location = tokens.next();
                tokens.assertNext("in");
                val duration = tokens.next().toLong();
                tokens.assertNext("ticks");
                val time = parseTime(tokens);

                return Move(obj, location, duration, time);
            }
        }
        override fun toString(): String {
            return "Move ${movedObject.toString()} to $location in $duration ticks at tick $time";
        }
    }
    data class Despawn(val entity: LoggedObject.Entity, override val time: Long) : LogCommand(time) {
        companion object {
            fun fromString(tokens: Tokens): Despawn {
                tokens.assertNext("Despawn");
                val entity = LoggedObject.Entity.fromString(tokens);
                val time = parseTime(tokens);

                return Despawn(entity, time);
            }
        }
        override fun toString(): String {
            return "Despawn ${entity.toString()} at tick $time";
        }
    }

    companion object {
        fun fromString(str: String): LogCommand {
            var tokens = Tokens(str);
            
            return when (tokens.peek()) {
                "Change" -> Change.fromString(tokens)
                "Push" -> PushQueueStack.fromString(tokens)
                "Pop" -> PopQueueStack.fromString(tokens)
                "Add" -> AddResource.fromString(tokens)
                "Pickup" -> PickupEntity.fromString(tokens)
                "Dropoff" -> DropoffEntity.fromString(tokens)
                "Move" -> Move.fromString(tokens)
                "Despawn" -> Despawn.fromString(tokens)
                else -> throw IllegalArgumentException("Invalid command type")
            }
        }
    }
}


fun testLine(inp: String, expected: LogCommand) {
    val res = LogCommand.fromString(inp);
    if (res != expected) {
        error("Got $res from $inp, expected: $expected");
    }

    val res2 = res.toString();
    if (res2 != inp) {
        error("Got $res2 from $res, expected: $inp");
    }
}
fun main() {
    testLine(
        "Change Variable var1 to 20 at tick 0",
        LogCommand.Change.Variable(
            "var1", null, null, 20, 0
        )
    )
    testLine(
        "Change Variable var1 at 1 20 to 30 at tick 10",
        LogCommand.Change.Variable(
            "var1", 1, 20, 30, 10
        )
    )

    testLine(
        "Change Image station1 to image1 at tick 20",
        LogCommand.Change.Image(
            LoggedObject.Unique("station1"), "image1", 20
        )
    )
    testLine(
        "Change Image Entity Person Ben to person1Image at tick 10",
        LogCommand.Change.Image(
            LoggedObject.Entity("Person", "Ben"),
            "person1Image", 10
        )
    )

    testLine(
        "Push Entity Person Ben onto stack1 at tick 30",
        LogCommand.PushQueueStack(
            LoggedObject.Entity("Person", "Ben"),
            "stack1", 30
        )
    )

    testLine(
        "Pop Entity Person Ben off of stack1 at tick 40",
        LogCommand.PopQueueStack(
            LoggedObject.Entity("Person", "Ben"),
            "stack1", 40
        )
    )

    testLine(
        "Add Resource resource1 to thing1 at tick 50",
        LogCommand.AddResource(
            "resource1", "thing1", 50
        )
    )

    testLine(
        "Pickup Entity Person Ben onto Transporter1 at tick 60",
        LogCommand.PickupEntity(
            LoggedObject.Entity("Person", "Ben"),
            "Transporter1",
            60
        )
    )

    testLine(
        "Dropoff Entity Person Ben from Transporter1 at tick 70",
        LogCommand.DropoffEntity(
            LoggedObject.Entity("Person", "Ben"),
            "Transporter1", 70
        )
    )

    testLine(
        "Move Transporter1 to station2 in 20 ticks at tick 80",
        LogCommand.Move(
            LoggedObject.Unique("Transporter1"), 
            "station2", 20, 80
        )
    )

    testLine(
        "Despawn Entity Person Bob at tick 90",
        LogCommand.Despawn(
            LoggedObject.Entity("Person", "Bob"),
            90
        )
    )
}