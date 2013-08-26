package fiu.kdrg.test;

import org.jblas.DoubleMatrix;

public class DoubleMatrixTest {

	
	public static void main(String[] args) {
		
		int N = 10;
		DoubleMatrix matrix = DoubleMatrix.zeros(N, N);
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(i != j)
					matrix.put(i,j,1);
			}
		}
		
		DoubleMatrix ind = matrix.getRow(4).ge(0.5);
		for(int i=0; i < ind.rows; i++){
			for(int j = 0; j < ind.columns; j++){
				System.out.println(ind.get(i, j));
			}
		}
		
	}
	
	
}
