# ***Download the program by going to the latest release on the right hand side of the screen and running the .jar file***  

**Simulates user defined equations including vector fields and complex-valued functions**  
  
**Creating a vector field:**  
  - Input the functions of the vector field such that the x component is in the "P" box and the y component is in the "Q" box. For example, P = y and Q = 0 would make the x component of the vectors proportional to the y value.  
  - The default colors of the vectors get brighter with increasing magnitude and vice versa. The hue (whether it's red, green, blue, yellow, etc.) is determined by the direction the arrow is pointing.  
  - The P and Q boxes can accept equations that depend on t (time), x (left to right of the screen), and y (down and up on the screen)  
  - You can zoom in and out of the vector field with a mouse scroll wheel or zooming in on a trackpad. You can also move the center of the vector field by dragging.
  - Add a test point using the button in the menu to see the path a point would take forward and backward in time if directed by the vector field
  
**Creating a complex equation:**  
  - To increase the detail (at the expense of performance) of the complex equation use the slider in the menu  
  - Complex equations can be made by putting any function of z (e.g. z<sup>2</sup>) into the "P" box.  
  - Complex equations take in 2 inputs (the x and y coordinate of a pixel on screen converted to real (x) and imaginary (y) values) and spit out 2 outputs.  
  - The outputs of the complex equation have an argument (direction) and modulus (magnitude). Similar to the vector fields, the argument controls the hue and the modulus controls the brightness.
  - Create contour integrals (integrals of complex functions) with the buttons in the menu for either a freeform, closed freeform, or circular contour. The value of the contour integral is shown in the upper left corner of the screen.
  
**Mandelbrot set info:**  
  - This is an extra thing I put in because it looks pretty. To view the set type "zm" into the "P" box. To increase the number of max iterations slide the slider for the mandelbrot set in the menu.  
  - The mandelbrot set is defined as follows: for every a + bi in the complex plane, there is a function f<sub>c</sub>(z) = z<sup>2</sup> + c where c = a + bi. Then f<sub>c</sub>(f<sub>c</sub>(c)) = (z<sup>2</sup> + c)<sup>2</sup> + c and so on. If, after infinite iterations, this does not diverge to infinity then a + bi is part of the mandelbrot set.  
  - The points inside the mandelbrot set are colored black and the ones not in the set are colored based on how many iterations of f<sub>c</sub>(f<sub>c</sub>(...)) it takes for the magnitude of the complex number to be greater than 2, which is the cutoff for convergence.


    <img width="2611" height="1936" alt="image of the mandelbrot set viewed with the program" src="https://github.com/user-attachments/assets/047ccd73-e107-4a92-abc2-fadb55a5b877" />
