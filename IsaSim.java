/**
 * RISC-V Instruction Set Simulator
 * 
 * @author Lau Kai Sing (laut9810@gmail.com)
 * 
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IsaSim {

	static int pc;
	static int reg[] = new int[32];
	// memory = data[] + prog[]
	static int data[] = new int[64];


	// The final simulator has to read a binary file containing RISC-V instructions.
	// What is the file type for the instruction?
	// Do I need to run file when running?

	// Here the first program hard coded as an array
	static int instructions[] = {
			// As minimal RISC-V assembler example
			0x00200093, // addi x1 x0 2
			0x00300113, // addi x2 x0 3
			0x002081b3, // add x3 x1 x2
			0x40208233, // sub x4 x1 x2
	};

	private static final String INPUT_FILE_NAME = "C:\\TEMP\\cottage.jpg";
    private static final String OUTPUT_FILE_NAME = "C:\\TEMP\\cottage_copy.jpg";

    public byte[] readAlternateImpl(String inputFileName) {
        System.out.println("Reading in binary file named : " + inputFileName);
        File file = new File(inputFileName);
        System.out.println("File size: " + file.length());
        byte[] result = null;
        try {
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            result = readAndClose(input);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        return result;
    }

    public void write(byte[] input, String outputFileName) {
        System.out.println("Writing binary file...");
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(outputFileName));
                output.write(input);
            } finally {
                output.close();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not found.");
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    byte[] readAndClose(InputStream input) {
        // carries the data from input to output :
        byte[] bucket = new byte[32 * 1024];
        ByteArrayOutputStream result = null;
        try {
            try {
                // Use buffering? No. Buffering avoids costly access to disk or network;
                // buffering to an in-memory stream makes no sense.
                result = new ByteArrayOutputStream(bucket.length);
                int bytesRead = 0;
                while (bytesRead != -1) {
                    // aInput.read() returns -1, 0, or more :
                    bytesRead = input.read(bucket);
                    if (bytesRead > 0) {
                        result.write(bucket, 0, bytesRead);
                    }
                }
            } finally {
                input.close();
                // result.close(); this is a no-operation for ByteArrayOutputStream
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return result.toByteArray();
    }

    /** Run the example. */
    public void readprogr(String[] args) {
        // BytesStreamsAndFiles test = new BytesStreamsAndFiles();
        // byte[] readAlternateImpl = test.read(INPUT_FILE_NAME);
        // read in the bytes
        byte[] fileContents = readAlternateImpl(INPUT_FILE_NAME);
        write(fileContents, OUTPUT_FILE_NAME);
    }// test.readAlternateImpl(INPUT_FILE_NAME);
    	
	


	public static String decToHex(int dec) {
		return Integer.toHexString(dec); // this turn decimal to hex, only 
		//used in default case
	}

	public static int getUnsignedInt(int x) {
		System.out.println("int: " + x);
		System.out.println("unsigned int: " + (x & 0xffffffff));
		return (x & 0xffffffff);
	}

	public static void main(String[] args) {

		System.out.println("Hello RISC-V World!");

		pc = 0;
		// reg[0] = 0; // Don't know if necessary

		for (;;) {

			int instr = instructions[pc];
			int opcode = instr & 0x7f;
			int rd = (instr >> 7) & 0x01f;
			int funct3 = (instr >> 12) & 0x7;
			int rs1 = (instr >> 15) & 0x01f;
			int rs2 = (instr >> 20) & 0x01f;
			int imm = (instr >> 25);
			int I_imm = rs2 + (imm << 5); // for I-type
			int U_imm = funct3 + (rs1 << 5) + (rs2 << 8) + (imm << 13);
			int S_imm = rd + (imm << 5);
			int B_imm = rd + (imm << 5);
			// (rd >> 1)&0xf + (imm < 4)&0x3f; // not sure about B-type
			// int J_imm = ;

			System.out.println("instr: 0x" + decToHex(instr) + " ");
			System.out.println("opcode: 0x" + decToHex(opcode) + " ");
			System.out.println("rd: " + rd + " ");
			System.out.println("fucnt3: " + funct3 + " ");
			System.out.println("rs1: " + rs1 + " ");
			System.out.println("rs2: " + rs2 + " ");
			System.out.println("imm: " + imm + " ");

			switch (opcode) {
			// fence, fence.i, ebreak, csrrw, csrrs, csrrc, csrrwi, 
			//csrrsi, csrrci can be ignored
					
			case 0x0: // template
				switch (funct3) {
				}
				break;

			case 0x037: // LUI load upper immediate (20bits)
				reg[rd] = U_imm & 0xfffff000;
				break;

			case 0x027: // AUIPC Add Upper Imm (20bits) to PC
				reg[1] = pc; // do i need this?
				pc = U_imm & 0xfffff000;
				reg[rd] = pc;
				break;

			case 0x06f: // JAL
				reg[rd] = pc + 4;
				pc = B_imm - 4;
				break;

			case 0x067: // JALR
				reg[rd] = pc + 4;
				pc = reg[rs1] + B_imm;
				break;

			case 0x063: // type: bench
				switch (funct3) {
				case 0b000:// BEQ
					if (reg[rs1] == reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b001:// BNE
					if (reg[rs1] != reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b100: // BLT
					if (reg[rs1] < reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b101: // BGE
					if (reg[rs1] >= reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b110: // BLTU
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b111:// BGEU
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				}

				break;
		    case 0x003: // type:load
			switch (funct3) {     
			case 0x000:        // LB
                	reg [rd] =  data [rs1+I_imm] ;
                	break;

                	case 0x001:        // LH
			reg [rd] =  ;
                	break;
			
			case 0x010:        // LW
			reg [rd] =  ;
                	break;
	
			case 0x100:        // LBU
                 	reg [rd] =  data [rs1+I_imm] ;
               		break;

     			case 0x101:       // LHU
             		reg [rd] =  ;
                	break;
}
// LB
				// LH
				// LW
				// LNU
				// LHU
				break;

			case 0x023: // type: store           
                		switch (funct3) {
                		case 0b000: // SB
                		data[rs1 + S_imm/4] = data[rs1 + S_imm/4]& 0xffffff00 + reg[rs2] & 0xff;
                		break;
                		case 0b001: // SH
                		data[rs1 + S_imm/4] = data[rs1 + S_imm/4]&0xffff0000 + reg[rs2] & 0xffff;
                		break;
                		case 0b010: // SW
                		data[rs1 + S_imm/4] = reg[rs2];
                		}
                		break;


			case 0x013: // type: immediate

				switch (funct3) {
				case 0b000: // ADDI
					reg[rd] = reg[rs1] + I_imm;
					break;
				case 0b010: // SLTI (set < immediate)
					if (reg[rs1] < I_imm) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b011: // SLTIU (set < imm unsigned)
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(I_imm)) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
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
				case 0b101: // SRLI & SRAI 
					if (imm != 0) { // SRL
						reg[rd] = reg[rs1] >> I_imm;
					} else { // SRA
						// as far as I know, 
						//shifting in java keep the sign, 
						//need checking
						reg[rd] = reg[rs1] >> I_imm;
						reg[rd] = (reg[rd] & 0xffffffff);
					}
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
				case 0b010: // SLT (set<)
					if (reg[rs1] < reg[rs2]) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b011: // SLTU (set< unsign)
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b100: // XOR
					reg[rd] = reg[rs1] ^ reg[rs2];
					break;
				case 0b101: // SRL & SRA 
					if (imm != 0) { // SRL
						reg[rd] = reg[rs1] >> reg[rs2];
					} else { // SRA
						// as far as I know, 
						// shifting in java keep the sign,
						// need checking
						reg[rd] = reg[rs1] >> reg[rs2];
						reg[rd] = (reg[rd] & 0xffffffff);
					}
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
				pc = instructions.length;
				// When the program ends with ecall you write another binary file containing the
				// content of your registers (the .res file).
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
			if (pc >= instructions.length) {
				break;
			}
		}

		System.out.println("Program exit");

	}

}
