# ConnectX
ConnectX is a bot that maximizes the chances of winning in ConnectX, a variant of Connect Four featuring a table with variable dimensions, theoretically infinite.

<p align="center">
  <img src="https://github.com/lombichh/connectx/blob/main/imgs/connectx.jpg" alt="connectx image"/>
</p>

## Compile
The code can be compiled by executing the "compile.bat" file.

## Execute
Run in the command line:

CXGame application:

- Human vs Computer.  In the connectx/ directory run:
    ```sh
	    java -cp ".." connectx.CXGame 6 7 4 connectx.LombEnis.LombEnis
    ```

- Computer vs Computer. In the connectx/ directory run:
    ```sh
	    java -cp ".." connectx.CXGame 6 7 4 connectx.LombEnis.LombEnis connectx.L1.L1
    ```

CXPlayerTester application:

- Output score only:
    ```sh
	    java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.LombEnis.LombEnis connectx.L1.L1
    ```

- Verbose output
    ```sh
	    java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.LombEnis.LombEnis connectx.L1.L1 -v
    ```

- Verbose output and customized timeout (1 sec) and number of game repetitions (10 rounds)
    ```sh
	    java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.LombEnis.LombEnis connectx.L1.L1 -v -t 1 -r 10
    ```