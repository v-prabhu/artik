class base {
  $dirs = [
    "/opt/artik",
    "/opt/artik/data",
    "/opt/artik/config",
    "/opt/artik/logs",
    "/opt/artik/templates",
    "/opt/artik/stacks" ]
  file { $dirs:
    ensure  => "directory",
    mode    => "755",
  }
  
  include artik
  include compose
}
