# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-printmapping out.map
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Preserve all annotations.

-keepattributes *Annotation*

-keepclassmembers class * {
    @javax.annotation.Resource *;
}

# You can print out the seeds that are matching the keep options below.

#-printseeds out.seeds

# Preserve all public servlets.

-keep public class * implements javax.servlet.Servlet
-keep public class * implements javax.servlet.ServletContextListener
-keep public class * implements javax.servlet.Filter

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Your application may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

# -keep class com.hiperf.common.ui.server.util.sequence.*
# -keep interface com.hiperf.common.ui.client.INakedObject
# -keep class com.hiperf.common.ui.shared.AbstractEntity
# -keep public class * extends com.hiperf.common.ui.shared.AbstractEntity
# -keep class * implements com.hiperf.common.ui.client.INakedObject
-keep class * implements com.google.gwt.user.client.rpc.IsSerializable
-keep class * extends com.google.gwt.core.ext.Generator
-keep class com.hiperf.common.ui.client.**
-keep interface com.hiperf.common.ui.client.**
-keep class com.hiperf.common.ui.shared.**
-keep class com.hiperf.common.ui.server.storage.impl.StorageHelper {
	public static void fillGetAllMap(java.util.Map,java.lang.Object);
}
-keep interface com.hiperf.common.ui.shared.**
-keep class com.hiperf.common.ui.server.tx.*
-keep class com.hiperf.common.ui.server.model.*
-keep interface com.hiperf.common.ui.server.tx.*
-keep class com.hiperf.common.ui.server.util.IOUtils {
	public static java.lang.String htmlEncode(java.lang.String);
}
-keep class com.hiperf.common.ui.server.storage.impl.StorageService {
	public static com.hiperf.common.ui.server.storage.IStorageService getInstance();
}
-keep class com.hiperf.common.ui.server.storage.impl.ImportService {
	public static com.hiperf.common.ui.server.storage.IImportService getInstance();
}
-keep interface com.hiperf.common.ui.server.storage.IImportValidator

-keepclassmembers class * {
    @javax.persistence.* *;
}
-keepclasseswithmembernames class * {
    @javax.persistence.* *;
}
-keep class com.hiperf.security.keymaker.Main {
	public static void main(java.lang.String[]);
}
