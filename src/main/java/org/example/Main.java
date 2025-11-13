package org.example;

import org.example.impl.JLLM;

public class Main {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        JLLM jllm = new JLLM();
        jllm.run();
    }

    public static void testSpoon() {
//
//        model.getAllTypes().forEach(type -> LOGGER.info(type.getQualifiedName()));
//        // 5. Extract and print all methods in the model
////        model.getElements(f -> f instanceof CtMethod<?>).forEach((m -> {
////            CtMethod method = (CtMethod) m;
////            System.out.println("Method: " + method.getSimpleName());
////            System.out.println("Signature: " + method.getSignature());
////            System.out.println("Declaring class: " + method.getDeclaringType().getQualifiedName());
////            System.out.println("Source: \n" + method);
////            System.out.println("------------------------------------------");
////        }));
//        // Retrieve all top-level classes, interfaces, etc.
//            Collection<CtType<?>> allTypes = model.getAllTypes();
//
//        // Open a writer to the output file.
//        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("methods_list.txt"))) {
//            long lastLogTime = System.currentTimeMillis();
//            long count = 0;
//            for (CtType<?> ctType : allTypes) {
//                for (CtMethod<?> ctMethod : ctType.getMethods()) {
//                    // Construct the line: "fully.qualified.ClassName.methodName"
//                    String line = ctType.getQualifiedName() + "." + ctMethod.getSimpleName();
//                    writer.write(line);
//                    writer.newLine();
//
//                    count++;
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime - lastLogTime >= 1000) { // 1 second has passed
//                        System.out.println("Processed " + count + " methods so far...");
//                        lastLogTime = currentTime;
//                    }
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }
}