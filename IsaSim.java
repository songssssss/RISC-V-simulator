/**
 * RISC-V Instruction Set Simulator
 * 
 * @author Lau Kai Sing (laut9810@gmail.com)
 * 
 */
public class IsaSim {

	static int pc;
	static int reg[] = new int[32];

	// memory?
	// static int mem[]
        
	// The final simulator has to read a binary file containing RISC-V instructions.
	// What is the file type for the instruction?
	// Do I need to run file when running?

	// Here the first program hard coded as an array
	private static final String INPUT_FILE_NAME = "C:\\TEMP\\cottage.jpg";
  	private static final String OUTPUT_FILE_NAME = "C:\\TEMP\\cottage_copy.jpg";

	
	byte[] readAlternateImpl(String inputFileName){
   	log("Reading in binary file named : " + inputFileName);
    	File file = new File(inputFileName);
    	log("File size: " + file.length());
    	byte[] result = null;
    	try {
      	InputStream input =  new BufferedInputStream(new FileInputStream(file));
      	result = readAndClose(input);
   	 }
    	catch (FileNotFoundException ex){
      	log(ex);
    	}
    	return result;
  	}
	
	
  	
	
        void write(byte[] input, String outputFileName){
   	log("Writing binary file...");
    	try {
      	OutputStream output = null;
      	try {
        output = new BufferedOutputStream(new FileOutputStream(outputFileName));
        output.write(input);
      	}
      	finally {
        output.close();
      	}
    	}
    	catch(FileNotFoundException ex){
      	log("File not found.");
    	}
    	catch(IOException ex){
      	log(ex);
   	 }
  	}
	
	/** Run the example. */
  	void readprogr (String[] args) {
    	BytesStreamsAndFiles test = new BytesStreamsAndFiles();
    	//read in the bytes
    	byte[] fileContents = test.read(INPUT_FILE_NAME);
    	//test.readAlternateImpl(INPUT_FILE_NAME);
    	}
	
	void writeprogr (String[] args) {
    	//write it back out to a different file name
    	test.write(fileContents, OUTPUT_FILE_NAME);
	}
	
	static int progr[] = {
			// As minimal RISC-V assembler example
			0x00200093, // addi x1 x0 2
			0x00300113, // addi x2 x0 3
			0x002081b3, // add x3 x1 x2
			0x40208233, // sub x4 x1 x2
	};

	public static String decToHex(int dec) {
		return Integer.toHexString(dec);
	}

	public static int getUnsignedInt(int x) {
		System.out.println("int: " + x);
		System.out.println("unsigned int: " + (x & 0x00000000ffffffffL));
		return x & 0x00000000ffffffffL;
	}

	public static void main(String[] args) {

		System.out.println("Hello RISC-V World!");

		pc = 0;
		// reg[0] = 0; // Don't know if necessary

		for (;;) {

			int instr = progr[pc];
			int opcode = instr & 0x7f;
			int rd = (instr >> 7) & 0x01f;
			int funct3 = (instr >> 12) & 0x7;
			int rs1 = (instr >> 15) & 0x01f;
			int rs2 = (instr >> 20) & 0x01f;
			int imm = (instr >> 25);
			int I_imm = rs2 + (imm << 5); // for I-type
			int U_imm = funct3 + (rs1 << 5) + (rs2 << 8) + (imm << 13);
			int S_imm = rd + (imm << 5);
			int B_imm = (rd >> 1) + (imm < 4); // not sure about B-type
			// int J_imm = ;

			System.out.println("instr: 0x" + decToHex(instr) + " ");
			System.out.println("opcode: 0x" + decToHex(opcode) + " ");
			System.out.println("rd: " + rd + " ");
			System.out.println("fucnt3: " + funct3 + " ");
			System.out.println("rs1: " + rs1 + " ");
			System.out.println("rs2: " + rs2 + " ");
			System.out.println("imm: " + imm + " ");

			switch (opcode) {
			// fence, fence.i, ebreak, csrrw, csrrs, csrrc, csrrwi, csrrsi, csrrci can be
			// ignored
			case 0x0: // type:
				switch (funct3) {
				}
				break;

			case 0x037: // LUI

				break;

			case 0x027: // AUIPC

				break;

			case 0x06f: // JAL
				reg[rd] = pc + 4;
				pc = B_imm - 4;
				break;

			case 0x067: // JALR:

				break;

			case 0x063: // type: bench
				switch (funct3) {
				case 0b000:// BEQ
					if (reg[rs1] == reg[rs2]) {

					}
					break;
				case 0b001:// BNE
					if (reg[rs1] != reg[rs2]) {

					}
					break;
				case 0b100: // BLT
					if (reg[rs1] < reg[rs2]) {

					}
					break;
				case 0b101: // BGE
					if (reg[rs1] >= reg[rs2]) {

					}
					break;
				case 0b110: // BLTU
					if (getUnsignedint(reg[rs1]) < getUnsignedint(reg[rs2])) {

					}
					break;
				case 0b111:// BGEU
					if (getUnsignedint(reg[rs1]) < getUnsignedint(reg[rs2])) {

					}
					break;
				}

				break;
			case 0x003: // type:load
				// where is the memory?
				// LB
				// LH
				// LW
				// LNU
				// LHU
				break;

			case 0x023: // type: store
				// SB
				// SH
				// SW

				break;

			case 0x013: // type: immediate

				switch (funct3) {
				case 0b000: // ADDI
					reg[rd] = reg[rs1] + I_imm;
					break;
				case 0b010: // SLTI (set < immediate)
					break;
				case 0b011: // SLTIU (set < set < imm unsigned)
					break;
				case 0b100: // XORI
					reg[rd] = reg[rs1] ^ I_imm;
					break;
				case 0b110: // ORI
					reg[rd] = reg[rs1] | I_imm;
					break;
				case 0b111: // ANDI
					reg[rd] = reg[rs1] & I_imm;
					break;
				case 0b001: // SLLI
					reg[rd] = reg[rs1] << I_imm;
					break;
				case 0b101: // SRLI & SRAI //not sure about the difference, so only implement SRLI
					reg[rd] = reg[rs1] >> I_imm;
					break;
				}
				break;

			case 0x033:
				switch (funct3) {
				case 0b000: // ADD & SUB
					reg[rd] = reg[rs1] - reg[rs2] + 2 * (1 - (imm >> 5)) * reg[rs2];
					break;
				case 0b001: // SLL
					reg[rd] = reg[rs1] << reg[rs2];
					break;
				case 0b010: // SLT (set<) not sure how to implement
				case 0b011: // SLTU (set< unsign)
				case 0b100: // XOR
					reg[rd] = reg[rs1] ^ reg[rs2];
					break;
				case 0b101: // SRL & SRA Don't know the difference between shift right and arithmetic shift
							// right, so I implemnted the SRL only
					reg[rd] = reg[rs1] >> reg[rs2];
					break;
				case 0b110: // OR
					reg[rd] = reg[rs1] | reg[rs2];
					break;
				case 0b111: // AND
					reg[rd] = reg[rs1] & reg[rs2];
					break;

				}
				break;

			case 0x073: // ecall
				pc = progr.length;
				// when I try ecall in https://www.kvakil.me/venus/, it outputs "Invalid ecall
				// 0".
				// Should I do the same?
				break;

			default:
				System.out.println("Opcode 0x" + decToHex(opcode) + " not yet implemented");
				break;
			}

			++pc; // We count in 4 byte words

			for (int i = 0; i < reg.length; ++i) {
				System.out.print(reg[i] + " ");
			}
			System.out.println();
			if (pc >= progr.length) {
				break;
			}
		}

		System.out.println("Program exit");

	}

}
