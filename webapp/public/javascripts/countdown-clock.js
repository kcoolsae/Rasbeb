/* countdown-clock.js
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

/*
 * Code for the countdown clock used in contests. Needs JQuery
 */

function countdown_clock ($container, seconds, limit) {

    function update_clock ($container, seconds, orange_limit, red_limit) {

        if (seconds <= 0) {
           $container.attr('class', 'black')
           $container.html("00:00")
        } else {
           var s = seconds % 60
           var m = (seconds - s) / 60
           var str = (m < 10 ? '0'+m : m) + ':' + (s < 10 ? '0'+s : s)
           $container.attr('class',
              seconds <= red_limit ? 'red' : seconds <= orange_limit ? 'orange' : 'green')
           $container.html(str)
        }
   }

   update_clock ($container,seconds,limit/3,limit/6)
   var countdown = setInterval (function() {
        seconds --
        update_clock ($container,seconds,limit/3,limit/6)
        if (seconds <= 0) clearInterval(countdown)
    }, 1000)
}
