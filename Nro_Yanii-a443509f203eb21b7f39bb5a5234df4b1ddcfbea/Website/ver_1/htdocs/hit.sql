-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th8 01, 2023 lúc 05:29 PM
-- Phiên bản máy phục vụ: 10.4.28-MariaDB
-- Phiên bản PHP: 8.0.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `beta`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `atm_bank`
--

CREATE TABLE `atm_bank` (
  `id` int(11) NOT NULL,
  `refNo` varchar(255) NOT NULL,
  `creditAmount` varchar(255) NOT NULL,
  `transactionDate` text NOT NULL,
  `description` text DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `atm_lichsu`
--

CREATE TABLE `atm_lichsu` (
  `id` int(11) NOT NULL,
  `user_nap` int(11) DEFAULT NULL,
  `thoigian` datetime DEFAULT NULL,
  `magiaodich` varchar(255) DEFAULT NULL,
  `sotien` text DEFAULT NULL,
  `status` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `baiviet_hoangvietdung`
--

CREATE TABLE `baiviet_hoangvietdung` (
  `id` int(11) NOT NULL,
  `account_id` text NOT NULL,
  `top_baiviet` int(11) NOT NULL,
  `new` text NOT NULL,
  `tieude` text NOT NULL,
  `noidung` text NOT NULL,
  `time` varchar(99) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `napthe`
--

CREATE TABLE `napthe` (
  `id` int(11) NOT NULL,
  `user_nap` varchar(255) DEFAULT NULL,
  `telco` varchar(50) DEFAULT NULL,
  `serial` varchar(50) DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `status` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `atm_bank`
--
ALTER TABLE `atm_bank`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `atm_lichsu`
--
ALTER TABLE `atm_lichsu`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `baiviet_hoangvietdung`
--
ALTER TABLE `baiviet_hoangvietdung`
  ADD PRIMARY KEY (`id`);

-- Chỉ mục cho bảng `napthe`
--
ALTER TABLE `napthe`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `atm_bank`
--
ALTER TABLE `atm_bank`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `atm_lichsu`
--
ALTER TABLE `atm_lichsu`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `baiviet_hoangvietdung`
--
ALTER TABLE `baiviet_hoangvietdung`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `cmt_hoangvietdung`
--
ALTER TABLE `cmt_hoangvietdung`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `napthe`
--
ALTER TABLE `napthe`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
