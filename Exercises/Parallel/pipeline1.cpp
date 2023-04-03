#include <omp.h>
#include <iostream>
#include <unistd.h>
#include <time.h>       /* time_t, struct tm, difftime, time, mktime */

using namespace std;

int main (int argc, char *argv[])
{
	int i, j;
	int ProcessingNum=10;    // 10 chunks
	time_t timer, timerEnd;

	time(&timer);
	#pragma omp parallel private(i) num_threads(4)
	{
		for (i=0; i<4; i++)    // 4 files to process
		{
			/* load data for iteration of the i-loop */
			#pragma omp single 
			{ 	
			        cout << "Thread: " << omp_get_thread_num() << " Reading ..." << endl; 
				// wait 4 seconds}
				usleep(1000*1000*4);
			} 

			#pragma omp for 
			for (j=0; j<ProcessingNum; j++)
			{   /* ProcessChunkOfData(); here is the work */
			    // consider each chunk takes 2 seconds
			    #pragma omp critical
			    cout  << "Thread: " << omp_get_thread_num() << " processing  i,j " << i << "," << j << endl;
			    usleep(1000*1000*2);
			}
			/* there is a barrier at the end of this loop */
			
			//{WriteResultsToFile(i);
			#pragma omp single 
			{ 	
			        cout << "Thread: " << omp_get_thread_num() << " Writing ..." << endl; 
				// wait 4 seconds}
				usleep(1000*1000*4);
			} 

		} 

	} /* one parallel region encloses all the work */

	time(&timerEnd);
	
  	cout << "Time: " << difftime(timerEnd,timer) << " seconds." << endl;

	return 0;
}