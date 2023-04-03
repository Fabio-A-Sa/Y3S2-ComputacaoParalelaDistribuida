#include <omp.h>
#include <iostream>
#include <unistd.h>
#include <time.h>       /* time_t, struct tm, difftime, time, mktime */

using namespace std;

// this program reads 4 files to process. Each file is processed in "10" chuncks
int main (int argc, char *argv[])
{
	int i, j, k=0;
	int ProcessingNum=10;
	time_t timer, timerEnd;

	time(&timer);

	#pragma omp parallel private(i) num_threads(4)
	{
		/* preload data to be used in first iteration of the i-loop */
		#pragma omp single 
		{ 	cout << "Thread: " << omp_get_thread_num() << " Reading ..." << k << endl;
			// wait 4 seconds}
			usleep(1000*1000*4);
		} 
	
		for (i=0; i<4; i++) {
			/* preload data for next iteration of the i-loop */
			#pragma omp single nowait
			{ 	if (k<4){
					k++;
					cout << "Thread: " << omp_get_thread_num() << " Reading ..." << k << endl; 
					// wait 4 seconds}
					usleep(1000*1000*4);
				}
			} 
		
			#pragma omp for schedule(dynamic)
			for (j=0; j<ProcessingNum; j++)
			{    /* ProcessChunkOfData(); here is the work */
			    // consider each chunk takes 2 seconds
			    #pragma omp critical
			    cout  << "Thread: " << omp_get_thread_num() << " processing  i,j " << i << "," << j << endl;
			    usleep(1000*1000*2);
			}
			/* there is a barrier at the end of this loop */

			#pragma omp single nowait
			{ 	cout << "Thread: " << omp_get_thread_num() << " Writing ..." << endl; 
				// wait 4 seconds}
				usleep(1000*1000*4);
			} 

		} /* threads immediately move on to next iteration of i-loop */

	} /* one parallel region encloses all the work */

	time(&timerEnd);
	
  	cout << "Time: " << difftime(timerEnd,timer) << " seconds." << endl;
	

	return 0;

}