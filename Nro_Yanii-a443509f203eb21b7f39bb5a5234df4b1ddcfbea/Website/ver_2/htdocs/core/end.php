
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
              <b><img src="<?=$logoend;?>" style="display: block; margin-left: auto; margin-right: auto; max-width:200px;animation: stretch 2s infinite alternate;"></b>
            </small>
            <small>
              <b style="color:black;"><?=$ten_server;?></b>
            </small>
            <br>
            <small style="color:black;">
              <span style="color:black;" id="currentYear"></span> © Được Vận Hành Bởi <b style="color:black;">
                <u><?=$doingu;?></u>
              </b>
            </small>
          </body>
        </div>
      </footer>
    </div>
  </body>		
</html>