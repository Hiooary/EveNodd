import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class encode extends fileMatrix{
	public static int LEN;

/******
 * 编码
 * @throws IOException 
 */	
public static int[][][] encode(int[][][] tempMemory) throws IOException
{
	int count=tempMemory.length;
	//System.out.print(count);
	
	int[][][] dataCache = new int[count][M-1][M];           //矩阵形式存储 {{1,0,1,1,0},{0,1,1,0,0},{1,1,0,0,0},{0,1,0,1,1}};
	int[][] tempMatrix1=new int[count][M-1];
	int[][]tempMatrix2=new int[count][M-1];//行校验和对角线校验
	int[][][] temp=new int[count][M-1][M+2];//存储校验数据后的盘符
	int s[] = new int[count];//奇偶校验符号
//	
	System.out.print("输出原数据："+"\n");
    display(tempMemory[0]);
//    
////	
	System.out.print("输出转存数据："+"\n");
	for(int i=0;i<count;i++)
	{
		dataCache[i]=getColumnData(tempMemory[i]);
		s[i]=getCommonFactor(dataCache[i]);
		tempMatrix1[i]=horiExclusive_OR(dataCache[i]);
		tempMatrix2[i]=diagExclusive_OR(dataCache[i],s[i]);
		temp[i]=matrixTransposition(tempMatrix1[i],tempMatrix2[i],dataCache[i]);
	}
	display(dataCache[0]);

	
//	s=getCommonFactor(dataCache);
//    System.out.print(s[0]+"\n");//求符号s
//	
//	//获取水平校验
//	tempMatrix1=horiExclusive_OR(dataCache);
//	for(int i=0;i<tempMatrix1[0].length;i++)
//	{
//		System.out.print(tempMatrix1[0][i]+" ");
//	}
//	System.out.print("\n");
//	//获取对角线校验
	//tempMatrix2=diagExclusive_OR(dataCache,s);
//	for(int i=0;i<tempMatrix2[0].length;i++){
//		System.out.print(tempMatrix2[0][i]+" ");
//	}
//	System.out.print("\n");
//	
//	//存储得到的校验盘
//	temp=matrixTransposition(tempMatrix1,tempMatrix2,dataCache);
	System.out.print("存储得到的校验盘\n");
	display(temp[0]);
	
//	//将编码后的结果以文件形式存储
//	DataOutputStream fpw1=new DataOutputStream(new FileOutputStream("./2-"+M+".jpg"));
//	DataOutputStream fpw2=new DataOutputStream(new FileOutputStream("./2-"+(M+1)+".jpg"));
//	
////	int len=0;
////	while(len <= LEN)
////	{
//		for(int i=0;i<tempMatrix1.length;i++)
//		{
//			for(int j=0;j<tempMatrix1[0].length;j++)
//			{
//				fpw1.writeByte(tempMatrix1[i][j]);
//				fpw2.writeByte(tempMatrix2[i][j]);
//			//	len++;
//			}	
//		}	
//	//}
//		fpw1.close();
//		fpw2.close();
		
	return temp;
}

/******
 * 译码
 * @throws IOException 
 */		
public static void decode(int error1,int error2,int[][][] dataCache) throws IOException
{
	int m=M;
	int count=dataCache.length;
	
	if(error1 != -1 && error2 !=-1)////两个磁盘出错
	{
		//错误的是两个校验块
		if(error1 == m && error2 == (m+1))
		{
			//此情况类同于编码
			//破坏数据,此处是置0/////////////////////////////////////////////////
			Destroy(error1,error2,dataCache);
//			for(int k=0;k<count;k++)
//			{
//				for(int i=0;i<dataCache[0].length;i++)
//				{
//					dataCache[k][i][error1]=0;
//					dataCache[k][i][error2]=0;
//				}
//			}
			
			//破坏后的数据
			System.out.print("破坏后的数据\n");
			display(dataCache[0]);
			
			//去除校验块
			int[][][] tempArray=new int[count][m-1][m];
			for(int k=0;k<count;k++)
			{
				for(int i=0;i<(m-1);i++)
				{
					for(int j=0;j<m;j++)
					{
						tempArray[k][i][j]=dataCache[k][i][j];
					}
				}
			}
			
			
			//进行编码
			int[] s=new int[count];
			int[][] tempMatrix1=new int[count][M-1];
			int[][] tempMatrix2=new int[count][M-1];
			for(int i=0;i<count;i++)
			{
				s[i]=getCommonFactor(tempArray[i]);
				tempMatrix1[i]=horiExclusive_OR(tempArray[i]);
				tempMatrix2[i]=diagExclusive_OR(tempArray[i],s[i]);
			}
			
			//将译码出的数据写回丢失数据
			for(int k=0;k<count;k++)
			{
				for(int i=0;i<dataCache[0].length;i++)
				{
					dataCache[k][i][error1]=tempMatrix1[k][i];
					dataCache[k][i][error2]=tempMatrix2[k][i];
				}
			}
			
			//将编码后的结果以文件形式存储
			DataOutputStream fpw1=new DataOutputStream(new FileOutputStream("./2-"+error1+".jpg"));
			DataOutputStream fpw2=new DataOutputStream(new FileOutputStream("./2-"+error2+".jpg"));
			for(int i=0;i<tempMatrix1.length;i++)
				{
					for(int j=0;j<tempMatrix1[0].length;j++)
					{
						fpw1.writeByte(tempMatrix1[i][j]);
						fpw2.writeByte(tempMatrix2[i][j]);
					}	
				}	
			fpw1.close();
			fpw2.close();
			
			//控制台输出				
			System.out.print("修复后的数据\n");
			display(dataCache[0]);
		}
		//错误的一个数据块和水平校验块,此时应该传入带有校验的矩阵
		else if((error1 >= 0 && error1 < m ) && error2 == m)
		{				
			//破坏数据,此处是置0，可以写成一个函数/////////////////////////////////////////////////
			Destroy(error1,error2,dataCache);
			//破坏后的数据
			System.out.print("破坏后的数据\n");
			display(dataCache[0]);
			
			//增加一个元素全为0的行
			int[][][] temp=new int[count][m][m+2];
			
			for(int c=0;c<count;c++)
			{
				temp[c]=addRow(dataCache[c],temp[c]);
			}
			
			
			int[] s=new int[count];
			for(int c=0;c<count;c++)
			{
				s[c]=temp[c][getMod((error1-1),m)][m+1];
				for(int j=0;j<m;j++)
				{
					s[c]=s[c]^temp[c][getMod((error1-j-1),m)][j];
				}
			}
			System.out.print(s[0]+" ");// 123
			
			//恢复error1,公式有误，无法恢复,修改公式后，可以恢复/////////////////////////////////////////////////////////////////////////////
			//并用文件格式输出	
//			for(int c=0;c<count;c++)
//			{
			int f=0;
				for(int k=0;k<temp[f].length-1;k++)
				{
					temp[f][k][error1]=s[f]^temp[f][getMod(error1+k,m)][m+1];
					//System.out.print(temp[f][k][error1]+" ");
					for(int l=0;l<temp[f].length;l++)
					{	
						if(l!=error1)
						{		
							temp[f][k][error1]=temp[f][k][error1]^temp[f][getMod(k+error1-l,m)][l];
					    }	
					}
					dataCache[f][k][error1]=temp[f][k][error1];
				}		
			//}
	
			//恢复error2,用水平校验公式
			//并用文件格式表示
			
			//去除校验块
			int[][][] tempArray=new int[count][m-1][m];
			for(int c=0;c<count;c++)
			{
				for(int i=0;i<tempArray[c].length;i++)
				{
					for(int j=0;j<tempArray[c][i].length;j++)
					{
						tempArray[c][i][j]=temp[c][i][j];
					}
				}
			}
			
			int[][] tempMatrix1=new int[count][];
			for(int c=0;c<count;c++)
			{
				tempMatrix1[c]=horiExclusive_OR(tempArray[c]);
			    for(int i=0;i<temp[c].length-1;i++)
				    {
				    	//temp[i][error2]=tempMatrix1[i];
				    	dataCache[c][i][error2]=tempMatrix1[c][i];
				    }
			}
			
		    //控制台输出
		    System.out.print("修复后的数据\n");
		    display(dataCache[0]);
			
		}
		//错误的一个数据块和对角线校验块
		else if((error1 >= 0 && error1 < m) && error2 == (m+1) )
		{
			//破坏数据,此处是置0/////////////////////////////////////////////////
			Destroy(error1,error2,dataCache);
			
			//破坏后的数据
			System.out.print("破坏后的数据\n");
			display(dataCache[0]);
			
			//根据水平校验公式恢复error1
			for(int c=0;c<count;c++)
			{
				for(int i=0;i<dataCache[c].length;i++)
				{
					dataCache[c][i][error1]=0;
					for(int j=0;j<dataCache[c][i].length-1;j++)
					{
						if(j != error1)
						  dataCache[c][i][error1]=dataCache[c][i][error1]^dataCache[c][i][j];
					}
				}	
			}
			
			//根据对角线公式恢复error2
			//去除校验块
			int[][][] tempArray=new int[count][m-1][m];
			for(int c=0;c<count;c++)
			{
				for(int i=0;i<tempArray[c].length;i++)
				{
					for(int j=0;j<tempArray[c][i].length;j++)
					{
						tempArray[c][i][j]=dataCache[c][i][j];
					}
				}	
			}
			
			int[] s=new int[count];
			int[][] tempMatrix2=new int[count][];
			for(int c=0;c<count;c++)
			{
				s[c]=getCommonFactor(tempArray[c]);
				tempMatrix2[c]=diagExclusive_OR(tempArray[c],s[c]);
				for(int i=0;i<tempMatrix2[c].length;i++)
			    {
			    	dataCache[c][i][error2]=tempMatrix2[c][i];
			    }
			}
			
			//输出
			 System.out.print("修复后的数据\n");
			 display(dataCache[0]);
			
		}
		//错误的两个数据块
		else if((error1 >= 0 && error1 < m) && (error2 >= 0 && error2 < m))
		{
			//破坏数据,此处是置0/////////////////////////////////////////////////
			Destroy(error1,error2,dataCache);
			
			//破坏后的数据
			System.out.print("破坏后的数据\n");
			display(dataCache[0]);
			
			//增加一个元素全为0的行
			int[][][] temp=new int[count][m][m+2];
			for(int c=0;c<count;c++)
			{
				temp[c]=addRow(dataCache[c],temp[c]);
			}		
			//display(temp[0]);
			
			int[] s=new int[count];//奇偶符号
			for(int c=0;c<count;c++)
			{
				s[c]=0;
				for(int l=0;l<m-1;l++)
				{
					s[c]=s[c]^dataCache[c][l][m]^dataCache[c][l][m+1];
				}
			}		
			//System.out.print(s[0]);
			
			
			//寻找水平综合征
			int[][] S0 = new int[count][m];
			for(int c=0;c<count;c++)
			{
				for(int u=0;u<m;u++)
				{
					S0[c][u]=0;
					for(int l=0;l<=m;l++)
					{
						if(l != error1 && l!= error2)
							S0[c][u]=S0[c][u]^temp[c][u][l];
					}
				}	
			}
			
//			for(int i=0;i<S0[0].length;i++)
//			{
//				System.out.print(S0[0][i]+" ");
//			}
			
			int[][] S1=new int[count][m];//对角线综合征
			for(int c=0;c<count;c++)
			{
				for(int u=0;u<m;u++)
				{
					S1[c][u]=s[c]^temp[c][u][m+1];
					for(int l=0;l<m;l++)
					{
						if(l != error1 && l!= error2){	
							S1[c][u]=S1[c][u]^temp[c][getMod(u-l,m)][l];
						}
					}
				}
			}
			
//			for(int i=0;i<S1[0].length;i++)
//			{
//				System.out.print(S1[0][i]);
//			}
			
			//通过步骤计算
			int temp_s;
			for(int c=0;c<count;c++)
			{
				s[c]=getMod((-(error2-error1)-1),m);
				temp_s=s[c];
				while(temp_s!=(m-1))
				{
					temp[c][temp_s][error2]=S1[c][getMod(error2+temp_s,m)]^temp[c][getMod(temp_s+(error2-error1),m)][error1];
					temp[c][temp_s][error1]=S0[c][temp_s]^temp[c][temp_s][error2];
					dataCache[c][temp_s][error2]=temp[c][temp_s][error2];
					dataCache[c][temp_s][error1]=temp[c][temp_s][error1];
					temp_s=getMod(temp_s-(error2-error1),m);
				}
			}
			
			System.out.print("修复后的数据\n");
			display(dataCache[0]);
		}
	}
	//只有一个数据块出错
	else if(error2 == -1 && error1 != -1)
	{
		
	}
	else
	{//错误数据块不能找到
		System.out.print("error:fail to find the error disk!!");
		System.exit(0);
	
    }
}
@SuppressWarnings("null")
public static void main(String[] args) throws IOException
	{
	    int[][] buffer=split();//分好的块
	    
	    //System.out.print(buffer[0].length);
	    
	    int count=0;
	    int LEN=buffer[0].length;
	    if((LEN)%(M-1)!=0)
	    	count= LEN / (M-1) + 1;
	    //System.out.print(count);
	    
	    int[][][] tempMemory=new int[count][M][M-1];//将数据划分为矩阵
	    int t=0;
		for(int k=0;k < count;k++)
		{
		    for(int i=0;i<M;i++)
		    {
		    	for(int j=0;j<M-1;j++)
		    	{
		    		//System.out.print(buffer[i][t]);
		    		//对最后的一个矩阵补齐行数
		    		if(t>=LEN)
		    		{
		    			tempMemory[k][i][j]=0;
		    		}
		    		else
		    			tempMemory[k][i][j]=buffer[i][t];
		    		t++;
		    	}			
			}
		 }
	    
		//输出
//	    for(int k=0;k<2;k++)
//	    {
//		    for(int i=0;i<M-1;i++)
//			    {
//			    	for(int j=0;j<M;j++)
//			    	{
//			    		System.out.print(tempMemory[k][i][j]+" ");
//			    	}
//			    	System.out.print("\n");
//			    }
//		    System.out.print("\n");
//	    }
//	  
		////编码
		int[][][] dataCache;//接收编码后的矩阵, 应该设置为运行编码后，在运行译码还是能用
		dataCache=encode(tempMemory);
		//译码
		//在没解决全局dataCache的情况下，先注释dataCache的文件输出
		
		//第二。四情况还不行
		
		int error1 = 2,error2 = 4;//错误位置  //2
		decode(error1,error2,dataCache);
		
//		
		//decode();
		//merge();
	}

}
