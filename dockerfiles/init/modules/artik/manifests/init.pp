class artik {

# creating artik.env
  file { "/opt/artik/config/artik.env":
    ensure  => "present",
    content => template("artik/artik.env.erb"),
    mode    => "644",
  }
}
