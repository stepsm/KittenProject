package javaBytecodeGenerator;

import java.util.Set;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;
import bytecode.NEWSTRING;

//commentiamo anche in italiano per nostra comodità
@SuppressWarnings("serial")
public class TestClassGenerator extends JavaClassGenerator {

	// tipo per runtime/String in kittenBytecode 
	private static types.Type KITTEN_STRING = ClassType.mk( runTime.String.class.getSimpleName() );

	ClassType clazz;
	
	
	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName() + "Test", // name of the class
			// the superclass of the Kitten Object class is set to be the Java java.lang.Object class
			clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "java.lang.Object",
			clazz.getName() + ".kit", // source file
			Constants.ACC_PUBLIC, // Java attributes: public!
			noInterfaces, // no interfaces
			new ConstantPoolGen()); // empty constant pool, at the beginning

		this.clazz = clazz;
		
		// we add the fixtures
		for (FixtureSignature fixture: clazz.getFixtures())
			if (sigs.contains(fixture))
				fixture.createFixture(this);
		
		// we add the tests
		for (TestSignature test : clazz.getTests().values())
			if (sigs.contains(test))
				test.createTest(this);
		
		this.generateMainBytecode();
	}

	private void generateMainBytecode() {

		//crea l'istruction list del main(per adesso vuota)
		InstructionList mainInstructionList = new InstructionList();

		//crea il metodo main tramite methodgen
		MethodGen methodGen = new MethodGen
				(Constants.ACC_PUBLIC | Constants.ACC_STATIC, // public and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ new org.apache.bcel.generic.ArrayType("java.lang.String", 1) },
				null, // parameters names: we do not care
				"main", // method's name
				this.getClassName(), // defining class
				mainInstructionList, // bytecode of the method
				this.getConstantPool()); // constant pool
		
		// variabili locali aggiunte al metodo main
		LocalVariableGen time = methodGen.addLocalVariable("time", Type.INT, null, null);	//
		LocalVariableGen testTime = methodGen.addLocalVariable("testTime", Type.INT, null, null);	//tempo per eseguire il test
		LocalVariableGen testPassedCount = methodGen.addLocalVariable("testPassedCount", Type.INT, null, null);	//contatore per i test passati
		
		// inizializzo var locali
		mainInstructionList.append(InstructionFactory.ICONST_0); // inizializza a 0
		mainInstructionList.append(new ISTORE(time.getIndex())); // mette la variabile time nelle variabili locali(l'indice in realtà)
		
		mainInstructionList.append(InstructionFactory.ICONST_0);
		mainInstructionList.append(new ISTORE(testTime.getIndex()));	
		
		mainInstructionList.append(InstructionFactory.ICONST_0);
		mainInstructionList.append(new ISTORE(testPassedCount.getIndex()));
		
		//ricevitore del metodo(System out) (in ordine, per javabytecode, dobbiamo avere il methodGen, le variabili locali e il ricevitore)
		mainInstructionList.append(getFactory().createGetStatic( "java/lang/System", "out", Type.getType(java.io.PrintStream.class)));
		
		//ora si inizia a generare la stringa da stampare concatenando poi le varie stringe ottenute e costruite
		mainInstructionList.append(new NEWSTRING("\n \nTest execution for class " + this.clazz.getName() + "\n").generateJavaBytecode(this));//aggiunge una nuova stringa
		
		//si salva il tempo in cui iniziano i test
		InstructionHandle startTime = mainInstructionList.getEnd();
		
		// crea la stringa che indica se i test sono passati o meno e chiama la funzione per creare "O"
		createMiddleString(mainInstructionList, testPassedCount.getIndex(), testTime.getIndex());
		
		//ora manca solo la stringa finale, esempio: "3 tests passed, 1 failed [5.39ms]"
        mainInstructionList.append(new NEWSTRING(" \n" ).generateJavaBytecode(this));
		
        // concatena
        mainInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                        "concat", // name of the method or constructor
                        KITTEN_STRING.toBCEL(), // return type
                        new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                        Constants.INVOKEVIRTUAL));
        
		// aggiunge il contatore dei test passati
		mainInstructionList.append(new ILOAD(testPassedCount.getIndex()));	//carica count nello stack
		
		// concatena l'intero
        mainInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                        "concat", // name of the method or constructor
                        KITTEN_STRING.toBCEL(), // return type
                        new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, // parameters types
                        Constants.INVOKEVIRTUAL));
        
        //pezzo di stringa finale
        mainInstructionList.append(new NEWSTRING(" tests passed, " ).generateJavaBytecode(this));
		
        // concatena
        mainInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                        "concat", // name of the method or constructor
                        KITTEN_STRING.toBCEL(), // return type
                        new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                        Constants.INVOKEVIRTUAL));
		
        //carica il numero di test totali
        mainInstructionList.append(new LDC(getConstantPool().addInteger(clazz.getTests().size())));
        // carico il numero di test passati
        mainInstructionList.append(new ILOAD(testPassedCount.getIndex())); 
        //li sottraggo
		mainInstructionList.append(InstructionFactory.ISUB);
		
		// concatena l'intero
        mainInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                        "concat", // name of the method or constructor
                        KITTEN_STRING.toBCEL(), // return type
                        new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, // parameters types
                        Constants.INVOKEVIRTUAL));
		
        //pezzo di stringa finale
        mainInstructionList.append(new NEWSTRING(" failed" ).generateJavaBytecode(this));
		
        // concatena
        mainInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                        "concat", // name of the method or constructor
                        KITTEN_STRING.toBCEL(), // return type
                        new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                        Constants.INVOKEVIRTUAL));
        
        //calcola il tempo iniziale(quando sono iniziati i test) ed il tempo fino a questo momento 
        calculateTime(mainInstructionList, startTime, mainInstructionList.getEnd(), time.getIndex());
        
		mainInstructionList.append(getFactory().createInvoke( KITTEN_STRING.toBCEL().toString(),
				"output", 
				org.apache.bcel.generic.Type.VOID, // return type
				org.apache.bcel.generic.Type.NO_ARGS, // parameters types
				Constants.INVOKEVIRTUAL)); //stampa la stringa
		
		mainInstructionList.append(InstructionFactory.createReturn(Type.VOID));
		
			
		//methodGen.setInstructionList(mainInstructionList);
		
		// we must always call these methods before the getMethod()
		// method below. They set the number of local variables and stack
		// elements used by the code of the method
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		this.addMethod(methodGen.getMethod());
	}

	// crea la stringa che indica se i test sono passati o meno
	private void createMiddleString(InstructionList mainInstructionList, int testPassedCountIndex, int testTimeIndex) {
		
		for (TestSignature test: clazz.getTests().values()) {
			
			InstructionList testInstructionList = new InstructionList();
		
			// recupera il nome del test
			testInstructionList.append(new NEWSTRING("\t- " + test.getName() + ": ").generateJavaBytecode(this));
			
			//concatena 
			testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(),
					"concat",
				    KITTEN_STRING.toBCEL(), 
				    new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
				    Constants.INVOKEVIRTUAL));
			
			testInstructionList.append(InstructionFactory.DUP);  //TODO ci va o no??
			
			// genera i test
			testInstructionList.append(generateTest(test, clazz.getFixtures()));
						
			// dup per confronto
            testInstructionList.append(InstructionFactory.DUP);
           
            //crea una nuova stringa "passed" da confrontare
            testInstructionList.append(new NEWSTRING("passed").generateJavaBytecode(this));

            // confronto tra la stringa "passed" e quella ritornata dal test
            testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                            "equals", // name of the method or constructor
                            Type.BOOLEAN, // return type
                            new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                            Constants.INVOKEVIRTUAL));
           
            // add somma i booleani del confronto(equals) per sapere quanti test sono passati
            testInstructionList.append(new ILOAD(testPassedCountIndex));	//carica count
            testInstructionList.append(new IADD());	//somma i due numeri sullo stack 
            testInstructionList.append(new ISTORE(testPassedCountIndex));	//salva in count
           
            // concatena
            testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                            "concat", // name of the method or constructor
                            KITTEN_STRING.toBCEL(), // return type
                            new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                            Constants.INVOKEVIRTUAL));
			
            //calcola il tempo iniziale(quando sono iniziati i test) ed il tempo fino a questo momento 
            calculateTime(testInstructionList, testInstructionList.getStart(), testInstructionList.getEnd(), testTimeIndex);
            
            testInstructionList.append(new NEWSTRING("\n").generateJavaBytecode(this));
            
            // concatena
            testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                            "concat", // name of the method or constructor
                            KITTEN_STRING.toBCEL(), // return type
                            new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                            Constants.INVOKEVIRTUAL));
            
			//aggiunge le istruzioni al main instruction list
			mainInstructionList.append(testInstructionList);

		}
		
	}
	
	//crea l'oggetto "O" di tipo C
	private InstructionList generateTest(TestSignature test, Set<FixtureSignature> fixtures) {
		
		InstructionList genTestInstructionList = new InstructionList();
		
		// Inizializzo un nuovo oggetto O di tipo C
		genTestInstructionList.append(getFactory().createNew(this.clazz.getName()));
			
		//duplichiamo l'oggetto perche serve sia per il suo costruttore che per passarlo alle fixture
		genTestInstructionList.append(InstructionFactory.DUP);		
		
		// chiamiamo il costruttore per creare l'oggetto O
		genTestInstructionList.append(getFactory().createInvoke( this.clazz.getName(),
				"<init>", 
				org.apache.bcel.generic.Type.VOID, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESPECIAL ));
		
		//cicliamo sulle fixture e gli passiamo O come argomento
		for (FixtureSignature fixture: clazz.getFixtures()) {		
			
			// duplichiamo O
			genTestInstructionList.append(InstructionFactory.DUP);
			
			//chiama tutte le fixture di C passando O come argomento
			genTestInstructionList.append(getFactory().createInvoke( this.clazz.getName() + "Test",
					fixture.getName(), 
					org.apache.bcel.generic.Type.VOID, 
					new org.apache.bcel.generic.Type[]{ clazz.toBCEL() },
					org.apache.bcel.Constants.INVOKESTATIC ));
			
		}
		
		//chiama il test passando O come argomento
		genTestInstructionList.append(getFactory().createInvoke( this.clazz.getName() + "Test",
				test.getName(),
				new org.apache.bcel.generic.ObjectType(runTime.String.class.getName()),
				new org.apache.bcel.generic.Type[]{ clazz.toBCEL() },
				org.apache.bcel.Constants.INVOKESTATIC ));
		
		//ritorna l'istruction list locale
		return genTestInstructionList;
	}

	//calcola il tempo
	private void calculateTime(InstructionList testInstructionList, InstructionHandle start, InstructionHandle end, int testTimeIndex) {
		
		//prende il tempo prima e dopo l'inizio del test o di tutti i test
		InstructionList firstTime = new InstructionList();
		InstructionList secondTime = new InstructionList();
		
		//ritorna il tempo fina ad adesso(dal 1 gennaio 1970)  nello stack
		firstTime.append(getFactory().createInvoke("java/lang/System",
				"currentTimeMillis", 
				org.apache.bcel.generic.Type.LONG, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESTATIC ));
		
		//converte il tempo ottenuto da long a int
		firstTime.append(InstructionConstants.L2I);
		
		//lo carica in locale 
		firstTime.append(new ISTORE(testTimeIndex));
		
		//ritorna il tempo fina ad adesso(dal 1 gennaio 1970)  nello stack
		secondTime.append(getFactory().createInvoke("java/lang/System",
				"currentTimeMillis", 
				org.apache.bcel.generic.Type.LONG, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESTATIC ));
		
		//converte il tempo ottenuto da long a int
		secondTime.append(InstructionConstants.L2I);
		
		//carica nello stack anche il tempo precedente
		secondTime.append(new ILOAD(testTimeIndex));
		
		// sottrae i tempi
		secondTime.append(InstructionFactory.ISUB);
		
		//salva il risultato nella variabile testTimeIndex  
		secondTime.append(new ISTORE(testTimeIndex));
		
		testInstructionList.append(start, firstTime);
		testInstructionList.append(end, secondTime);
		
		// inseriamo il tempo formattato nel modo corretto
		testInstructionList.append(new NEWSTRING(" [" ).generateJavaBytecode(this));
		
		//concatena
		testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                "concat", // name of the method or constructor
                KITTEN_STRING.toBCEL(), // return type
                new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                Constants.INVOKEVIRTUAL));
		
		//carica il tempo
		testInstructionList.append(new ILOAD(testTimeIndex));
		
		// concatena l'intero
        testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
        		"concat", // name of the method or constructor
                KITTEN_STRING.toBCEL(), // return type
                new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, // parameters types
                Constants.INVOKEVIRTUAL));
        
        testInstructionList.append(new NEWSTRING("ms]" ).generateJavaBytecode(this));
		
		//concatena
		testInstructionList.append(getFactory().createInvoke(KITTEN_STRING.toBCEL().toString(), // name of the class
                "concat", // name of the method or constructor
                KITTEN_STRING.toBCEL(), // return type
                new org.apache.bcel.generic.Type[] { KITTEN_STRING.toBCEL() }, // parameters types
                Constants.INVOKEVIRTUAL));
		
	}
	
}











