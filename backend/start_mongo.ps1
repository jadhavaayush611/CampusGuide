$cp = Get-Content cp.txt
java -cp ($cp + ";target/classes;target/test-classes") com.campusguide.EmbeddedMongoRunner
