<!-- footer -->
<footer class="mt-1">
        <br>
        <div class="text-center text-black">
          <script>
            function getYear() {
              var date = new Date();
              var year = date.getFullYear();
              document.getElementById("currentYear").innerHTML = year;
            }
          </script>
          <body onload="getYear()">
            <small>
              <b><?=$server_name;?></b>
            </small>
            <br>
            <small>
              <span id="currentYear"></span> © Được Vận Hành Bởi <b> ADMIN
                <u></u>
              </b>
            </small>
          </body>
        </div>
      </footer>
    </div>
  </body>		
</html>